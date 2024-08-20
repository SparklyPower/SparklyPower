package net.perfectdreams.dreamchat.listeners

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcasamentos.DreamCasamentos
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.dao.ChatUser
import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamchat.tables.PremiumUsers
import net.perfectdreams.dreamchat.utils.*
import net.perfectdreams.dreamchat.utils.chatevent.EventoChatCalcular
import net.perfectdreams.dreamchat.utils.chatevent.EventoChatDesembaralhar
import net.perfectdreams.dreamchat.utils.chatevent.EventoChatMensagem
import net.perfectdreams.dreamchat.utils.chatevent.IEventoChat
import net.perfectdreams.dreamclubes.tables.PlayerDeaths
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.KDWrapper
import net.perfectdreams.dreamcore.dao.DiscordAccount
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.tables.DiscordAccounts
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.DreamUtils.jsonParser
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.girl
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.broadcastMessage
import net.perfectdreams.pantufa.rpc.GetDiscordUserRequest
import net.perfectdreams.pantufa.rpc.GetDiscordUserResponse
import net.perfectdreams.pantufa.rpc.PantufaRPCRequest
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import kotlin.collections.set

class ChatListener(val m: DreamChat) : Listener {
	val chatCooldownCache = Caffeine.newBuilder()
		.expireAfterWrite(1L, TimeUnit.MINUTES)
		.build<Player, Long>()
		.asMap()

	val lastMessageCache = Caffeine.newBuilder()
		.expireAfterAccess(1L, TimeUnit.MINUTES)
		.build<Player, String>()
		.asMap()

	var lastPantufaTimeout = 0L

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction {
				val result = ChatUsers.select { ChatUsers.id eq e.player.uniqueId }.firstOrNull() ?: return@transaction
				val nickname = result[ChatUsers.nickname] ?: return@transaction

				e.player.setDisplayName(nickname)
				e.player.setPlayerListName(nickname)
			}
		}
	}

	@EventHandler
	fun onLeave(e: PlayerQuitEvent) {
		m.lockedTells.remove(e.player)
		chatCooldownCache.remove(e.player)
		lastMessageCache.remove(e.player)
		m.hideTells.remove(e.player)
	}

	@EventHandler
	fun onTag(e: ApplyPlayerTagsEvent) {
		if (e.player.uniqueId == m.eventoChat.lastWinner) {
			e.tags.add(
				PlayerTag(
					"§b§lD",
					"§b§lDatilógraf${e.player.artigo}",
					listOf(
						"§r§b${e.player.displayName}§r§7 ficou atento no chat e",
						"§7e preparad${e.player.artigo} no teclado para conseguir",
						"§7vencer o Evento Chat em primeiro lugar!"
					),
					null,
					false
				)
			)
		}

		// Get top player
		runBlocking {
			net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
				val killedCount = PlayerDeaths.killed.count()
				val killeds = PlayerDeaths.slice(PlayerDeaths.killed, killedCount)
					.selectAll()
					.groupBy(PlayerDeaths.killed)
					.toList()

				val killerCount = PlayerDeaths.killed.count()
				val killers = PlayerDeaths.slice(PlayerDeaths.killer, killerCount)
					.selectAll()
					.groupBy(PlayerDeaths.killer)
					.toList()

				val ids = (killeds.map { it[PlayerDeaths.killed] } + killers.mapNotNull { it[PlayerDeaths.killer] }).toSet()

				val kdrs = mutableMapOf<UUID, KDWrapper>()
				for (id in ids) {
					val playerDeathCount = killeds.firstOrNull { it[PlayerDeaths.killed] == id }?.get(killedCount) ?: 0
					val playerKillCount = killers.firstOrNull { it[PlayerDeaths.killer] == id }?.get(killerCount) ?: 0
					val kdrWrapper = KDWrapper(playerKillCount, playerDeathCount)
					kdrs[id] = kdrWrapper
				}

				val top = kdrs.entries.maxByOrNull { it.value.getRatio() }
				if (top?.key == e.player.uniqueId) {
					e.tags.add(
						PlayerTag(
							"§c§lM",
							"§c§lMMestre",
							listOf(
								"§r§b${e.player.displayName}§r§7 é o mestre do PvP",
								"§7e possui o maior KDR do servidor!"
							),
							null,
							false
						)
					)
				}
			}
		}

		if (e.player.uniqueId.toString() == m.config.get("last-enderdragon-killer")) {
			val gender = e.player.artigo == "a"

			e.tags.add(
				PlayerTag(
					"§a§lC",
					"§a§l${if(gender) "Caçadora" else "Caçador"}",
					listOf(
						"§r§9${e.player.displayName}§r§7 conseguiu matar o Ender Dragon! ${if(gender) "Ela" else "Ele"}",
						"§7mostrou a todos suas habilidades como ${if(gender) "caçadora" else "caçador"},",
						"§7trazendo para casa a cabeça do temido dragão e também uma linda tag!"
					),
					null,
					false
				)
			)
		}
	}

	@EventHandler
	fun onJoin(e: PlayerCommandPreprocessEvent) {
		if (!m.eventoChat.running)
			return

		val cmd = e.message
			.split(" ")[0]
			.substring(1)
			.toLowerCase()

		if (cmd == "calc" || cmd == "calculadora")
			e.isCancelled = true
	}

	@EventHandler
	fun onEnderDragonDeathEvent(e: EntityDeathEvent) {
		if (e.entity is EnderDragon) {
			var whoKilled = e.entity.killer

			if (whoKilled == null) {
				val killerEntity = e.entity.lastDamageCause as EntityDamageByEntityEvent

				if (killerEntity is Player)
					whoKilled = killerEntity.damager as Player

				if (killerEntity is Projectile) {
					val projectile = killerEntity.damager as Projectile

					if (projectile.shooter is Player)
						whoKilled = projectile.shooter as Player
				}
			}

			var enderDragonKilledMessage = "§8[§5§lThe End§8] §c§lO dragão do do The End morreu por causa de alguma §6§lexplosão§c§l, parabéns a todos!"

			if (whoKilled !== null) {
				m.config.set("last-enderdragon-killer", whoKilled.uniqueId.toString())
				m.saveConfig()

				val enderDragonEgg = ItemStack(Material.DRAGON_EGG, 1)

				whoKilled.inventory.addItem(enderDragonEgg)

				enderDragonKilledMessage = "§8[§5§lThe End§8] §c§lO Dragão do The End foi morto por §6§l${whoKilled.name}§c§l, parabéns!"
			}

			Bukkit.broadcastMessage(enderDragonKilledMessage)
		}
	}

	data class PlayerMessage(
		val player: Player,
		val message: String
	)

	val messageCache = mutableListOf<PlayerMessage>()
	val maxCacheSize = 10
	var lastEventMessage: String? = null

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onChat(e: AsyncPlayerChatEvent) {
		e.isCancelled = true

		val lockedTellPlayer = m.lockedTells[e.player]
		if (lockedTellPlayer != null) {
			if (Bukkit.getPlayerExact(lockedTellPlayer) != null) {
				scheduler().schedule(m) {
					e.player.performCommand("tell $lockedTellPlayer ${e.message}")
				}
				return
			} else {
				e.player.sendMessage("§cO seu chat travado foi desativado devido á saida do player §b${lockedTellPlayer}§c")
				e.player.sendMessage("§cPor segurança, nós não enviamos a sua última mensagem, já que ela iria para o chat normal e não para a sua conversa privada")
				e.isCancelled = true
				m.lockedTells.remove(e.player)
				return
			}
		}

		val player = e.player
		var message = e.message

		if (m.eventoChat.running) {
			lastEventMessage = m.eventoChat.event.getAnswer()

            // We need to check if it's a chat event message to avoid triggering the anti-raid system
			if (message.equals(lastEventMessage, true)) {
                m.logger.info { "Detected event chat message ($lastEventMessage). Skipping anti-raid verification" }

				if (m.eventoChat.event.process(player, message)) {
					m.eventoChat.finish(player)
				}
			}
		}

		if (!message.equals(lastEventMessage, true)) {
            synchronized(messageCache) {
                if (messageCache.size >= maxCacheSize) {
                    messageCache.removeAt(0)
                }

                val playersWithSameMessage = messageCache
                    .filter { it.message.equals(message, true) }
                    .map { it.player.name }
                    .toMutableList()

                if (!playersWithSameMessage.contains(player.name)) {
                    playersWithSameMessage.add(player.name)
                    messageCache.add(PlayerMessage(player, message))
                }

                val messageCount = synchronized(messageCache) {
                    messageCache
                        .filter { it.message.equals(message, true) }
                        .map { it.player.name }
                        .toSet()
                        .size
                }

                if (messageCount >= 3) {
                    DreamNetwork.PANTUFA.sendMessageAsync(
                        "1274126432691552429",
                        "**${
                            player.name.replace(
                                "_",
                                "\\_"
                            )
                        }** provavelmente está raidando o servidor, mensagem enviada pelo usuário: ```\n$message\n``` <@&332650495522897920>"
                    )
                    e.isCancelled = true
                    return
                }
            }
        }

		val lastMessageSentAt = chatCooldownCache.getOrDefault(player, 0)
		val diff = System.currentTimeMillis() - lastMessageSentAt

		if (500 >= diff) {
			player.sendMessage("§cEspere um pouco para enviar outra mensagem no chat!")
			return
		}

		if (3500 >= diff) {
			val lastMessageContent = lastMessageCache[player]
			if (lastMessageContent != null) {
				if (5 > StringUtils.getLevenshteinDistance(lastMessageContent, message) && !m.eventoChat.running) {
					player.sendMessage("§cNão mande mensagens iguais ou similares a última que você mandou!")
					return
				}
			}
		}

		val upperCaseChars = e.message.toCharArray().filter { it.isUpperCase() }
		val upperCasePercent = (e.message.length * upperCaseChars.size) / 100

		if (e.message.length >= 20 && upperCasePercent > 55) {
			player.sendMessage("§cEvite usar tanto CAPS LOCK em suas mensagens! Isso polui o chat!")
			e.message = e.message.toLowerCase()
		}

		chatCooldownCache[player] = System.currentTimeMillis()
		lastMessageCache[player] = e.message.toLowerCase()

		// Vamos verificar se o cara só está falando o nome do cara da Staff
		for (onlinePlayers in Bukkit.getOnlinePlayers()) {
			if (onlinePlayers.hasPermission("sparklypower.soustaff")) {
				if (message.equals(onlinePlayers.name, true)) {
					player.sendMessage("§cSe você quiser chamar alguém da Staff, por favor, coloque a pergunta JUNTO com a mensagem, obrigado! ^-^")
					return
				}
			}
		}

		message = message.translateColorCodes()

		if (!player.hasPermission("dreamchat.chatcolors")) {
			message = message.replace(DreamChat.CHAT_REGEX, "")
		}

		if (!player.hasPermission("dreamchat.chatformatting")) {
			message = message.replace(DreamChat.FORMATTING_REGEX, "")
		}

		if (ChatUtils.isMensagemPolemica(message)) {
			DreamNetwork.PANTUFA.sendMessageAsync(
				"387632163106848769",
				"**`" + player.name.replace("_", "\\_") + "` escreveu uma mensagem potencialmente polêmica no chat!**\n```" + message + "```\n"
			)
		}

		if (message.startsWith("./") || message.startsWith("-/")) { // Caso o player esteja dando um exemplo de um comando, por exemplo, "./survival"
			message = message.removePrefix(".")
		}

		message = ChatUtils.beautifyMessage(player, message)

		// Hora de "montar" a mensagem
		val textComponent = TextComponent()

		val playOneMinute = player.getStatistic(Statistic.PLAY_ONE_MINUTE)

		var prefix = VaultUtils.chat.getPlayerPrefix(player)

		val api = LuckPermsProvider.get()

		val luckyUser = api.userManager.getUser(e.player.uniqueId)

		val chatUser = transaction(Databases.databaseNetwork) {
			ChatUser.find {
				ChatUsers.id eq e.player.uniqueId
			}.firstOrNull()
		}

		if ((luckyUser?.primaryGroup ?: "default") == "default" && (7200 * 20) > playOneMinute) {
			prefix = if (e.player.girl) {
				"§eNovata"
			} else {
				"§eNovato"
			}
		}

		if (chatUser != null) {
			if (chatUser.nickname != null && !e.player.hasPermission("dreamchat.nick")) {
				transaction(Databases.databaseNetwork) {
					chatUser.nickname = null
				}
				e.player.setDisplayName(null)
				e.player.setPlayerListName(null)
			}

			if (chatUser.tag != null && !e.player.hasPermission("dreamchat.querotag")) {
				transaction(Databases.databaseNetwork) {
					chatUser.tag = null
				}
			}

			if (chatUser.tag != null) {
				prefix = chatUser.tag
			}
		}

		textComponent += "§8[${prefix.translateColorCodes()}§8] ".toTextComponent().apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "kk eae men".toBaseComponent())
		}

		val event = ApplyPlayerTagsEvent(player, mutableListOf())
		Bukkit.getPluginManager().callEvent(event)

		if (m.topEntries[0] == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§2§lM",
					"§2§lMagnata",
					listOf(
						"§r§b${player.displayName}§r§7 é a pessoa mais rica do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		if (m.topEntries[1] == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§2§lL",
					"§2§lLuxuos${player.artigo}",
					listOf(
						"§r§b${player.displayName}§r§7 é a segunda pessoa mais rica do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		if (m.topEntries[2] == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§2§lB",
					"§2§l${if (!player.girl) { "Burguês" } else { "Burguesa" }}",
					listOf(
						"§r§b${player.displayName}§r§7 é a terceira pessoa mais rica do §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/money top",
					true
				)
			)
		}

		McMMOTagsUtils.addTags(e, event)

		if (m.oldestPlayers.getOrNull(0)?.first == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§4§lV",
					"§4§lViciad${player.artigo}",
					listOf(
						"§r§b${player.displayName}§r§7 é a pessoa com mais tempo online no §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/online",
					true
				)
			)
		}

		if (m.oldestPlayers.getOrNull(1)?.first == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§4§lD",
					"§4§lDevotad${player.artigo}",
					listOf(
						"§r§b${player.displayName}§r§7 é a segunda pessoa com mais tempo online no §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/online",
					true
				)
			)
		}

		if (m.oldestPlayers.getOrNull(2)?.first == e.player.uniqueId) {
			event.tags.add(
				PlayerTag(
					"§4§lF",
					"§4§lFanátic${player.artigo}",
					listOf(
						"§r§b${player.displayName}§r§7 é a terceira pessoa com mais tempo online no §4§lSparkly§b§lPower§r§7!",
						"",
						"§7Eu duvido você conseguir passar del${if (player.girl) "a" else "e"}, será que você tem as habilidades para conseguir? ;)"
					),
					"/online",
					true
				)
			)
		}

		if (event.tags.isNotEmpty()) {
			// omg tags!
			// Exemplos:
			// Apenas uma tag
			// [Último Votador]
			// Duas tags
			// [DS]
			//
			// Para não encher o chat de tags
			// Todas as tags são EXTENDIDAS por padrão
			// Mas, se o cara tiver mais de uma tag, todas ficam ENCURTADAS
			val textTags = "§8[".toTextComponent()

			val tags = event.tags

			if (tags.size == 1) {
				for ((index, tag) in tags.withIndex()) {
					val textTag = tag.tagName.toTextComponent().apply {
						if (tag.description != null) {
							hoverEvent = HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								"§6✪ §f${tag.tagName} §6✪\n§7${tag.description.joinToString("\n§7")}".toBaseComponent()
							)
						}
						if (tag.suggestCommand != null) {
							clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tag.suggestCommand)
						}
					}
					textTags += textTag
				}
			} else {
				// We have multiple tags that can be used, we will select the one that has the most tags assigned to it
				val wordTags = listOf(
					WordTagFitter("SPARKLYPOWER"),
					WordTagFitter("SPARKLY"),
					WordTagFitter("POWER"),
					WordTagFitter("LORITTA"),
					WordTagFitter("PANTUFA"),
					WordTagFitter("FELIZ"),
					WordTagFitter("DILMA"),
					WordTagFitter("CRAFT"),
					WordTagFitter("LORI"),
					WordTagFitter("MINE"),
					WordTagFitter("DIMA")
				)

				wordTags.forEach { wordTag ->
					tags.forEach { tag ->
						wordTag.tryFittingInto(tag)
					}
				}

				val whatTagShouldBeUsed = wordTags.maxByOrNull {
					it.tagPositions.filterNotNull().distinct().size
				}

				// Add the tags from the whatTagShouldBeUsed tag fitter to a "displayInOrderTags"
				// Then we append the player's tags and do a .distinct() over it
				// So technically the "word tag fitter" will form a entire word, woo!
				val displayInOrderTags = if (whatTagShouldBeUsed == null)
					tags
				else (
						whatTagShouldBeUsed.tagPositions
							.filterNotNull()
								+ tags
						).distinct()

				for (tag in displayInOrderTags) {
					textTags += tag.small.toTextComponent().apply {
						if (tag.description != null) {
							hoverEvent = HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								"§6✪ §f${tag.tagName} §6✪\n§7${tag.description.joinToString("\n§7")}".toBaseComponent()
							)
						}
						if (tag.suggestCommand != null) {
							clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tag.suggestCommand)
						}
					}
				}
			}

			textTags.addExtra("§8] ")
			textComponent += textTags
		}

		/* val panela = DreamPanelinha.INSTANCE.getPanelaByMember(player)
		if (panela != null) {
			val tag = "§8«§3${panela.tag}§8» ".toTextComponent()
			tag.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
					"""${panela.name}
						|§eDono: §b${panela.owner}
						|§eMembros: §6${panela.members.size}
						|§eKDR: §6${panela.calculateKDR()}
					""".trimMargin().toBaseComponent())
			textComponent += tag
		} */

		val clube = ClubeAPI.getPlayerClube(player)

		if (clube != null) {
			val clubeTag = "§8«§7${clube.shortName}§8» "

			textComponent += clubeTag.toTextComponent().apply {
				this.hoverEvent = HoverEvent(
					HoverEvent.Action.SHOW_TEXT,
					"§b${clube.name}".toBaseComponent()
				)
			}
		}

		val casal = DreamCasamentos.INSTANCE.getMarriageFor(player)
		if (casal != null) {
			val heart = "§4❤ ".toTextComponent()
			val offlinePlayer1 = Bukkit.getOfflinePlayer(casal.player1)
			val offlinePlayer2 = Bukkit.getOfflinePlayer(casal.player2)

			heart.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "§4❤ §d§l${DreamCasamentos.INSTANCE.getShipName(offlinePlayer1?.name ?: "???", offlinePlayer2?.name ?: "???")} §4❤\n\n§6Casad${MeninaAPI.getArtigo(player)} com: §b${Bukkit.getOfflinePlayer(casal.getPartnerOf(player))?.name ?: "???"}".toBaseComponent())
			textComponent += heart
		}

		textComponent += TextComponent(*"§7${player.displayName}".translateColorCodes().toBaseComponent()).apply {
			clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "${player.name} ")
			var toDisplay = player.displayName

			if (!player.displayName.stripColors()!!.contains(player.name)) {
				toDisplay = player.displayName + " §a(§b${player.name}§a)§r"
			}

			val input = playOneMinute / 20
			val numberOfDays = input / 86400
			val numberOfHours = input % 86400 / 3600
			val numberOfMinutes = input % 86400 % 3600 / 60

			val rpStatus = if (player.resourcePackStatus == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
				"§a✔"
			} else {
				"§c✗"
			}

			val isMinecraftPremium = transaction(Databases.databaseNetwork) {
				PremiumUsers.select { PremiumUsers.crackedUniqueId eq player.uniqueId }
					.count() != 0L
			}

			val mcPremiumStatus = if (isMinecraftPremium) {
				"§a✔"
			} else {
				"§c✗"
			}

			val mcBedrockEditionStatus = if (e.player.isBedrockClient) {
				"§a✔"
			} else {
				"§c✗"
			}

			// The runBlocking is required for the getPlayerKD call
			// Because this is called async, I don't think there is any issues in blocking
			val viaVersion = Via.getAPI()
			val playerVersion = ProtocolVersion.getProtocol(viaVersion.getPlayerVersion(e.player)).name
			val aboutLines = runBlocking {
				val numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("pt"))
				mutableListOf(
					"§6✪ §a§lSobre ${player.artigo} §r§b${toDisplay}§r §6✪",
					"",
					"§eGênero: §d${
						if (!player.girl) {
							"§3♂"
						} else {
							"§d♀"
						}
					}",
					"§eGrana: §6${numberFormat.format(player.balance)} Sonecas",
					"§eKDR: §6${numberFormat.format(ClubeAPI.getPlayerKD(e.player.uniqueId).getRatio())}",
					"§eOnline no SparklyPower Survival por §6$numberOfDays dias§e, §6$numberOfHours horas §ee §6$numberOfMinutes minutos§e!",
					"§eVersão do Minecraft:§6 ${if (e.player.isBedrockClient) { "Minecraft: Bedrock Edition (emulando $playerVersion)" } else { "Minecraft $playerVersion" }}",
					"§eUsando a Resource Pack: $rpStatus",
					"§eMinecraft Original: $mcPremiumStatus",
					"§eMinecraft: Bedrock Edition: $mcBedrockEditionStatus",
				)
			}

			val discordAccount = transaction(Databases.databaseNetwork) {
				DiscordAccount.find { DiscordAccounts.minecraftId eq player.uniqueId and (DiscordAccounts.isConnected eq true) }.firstOrNull()
			}

			if (discordAccount != null) {
				try {
					val bodyContent = if (System.currentTimeMillis() - lastPantufaTimeout >= 15_000) {
						runBlocking {
							// Timeout for when Pantufa is offline
							withTimeoutOrNull(250) {
								DreamUtils.http.post("http://pantufa.tail2f90.ts.net:25665/rpc") {
									setBody(
										TextContent(
											Json.encodeToString<PantufaRPCRequest>(
												GetDiscordUserRequest(
													discordAccount.discordId
												)
											), ContentType.Application.Json
										)
									)
								}.bodyAsText()
							}
						}
					} else null

					if (bodyContent != null) {
						when (val response = Json.decodeFromString<GetDiscordUserResponse>(bodyContent)) {
							GetDiscordUserResponse.NotFound -> {} // Unknown user, bail out
							is GetDiscordUserResponse.Success -> aboutLines.add("§eDiscord: §6${response.name}§x§c§c§8§8§0§0#${response.discriminator} §8(§7${discordAccount.discordId}§8)")
						}
					} else {
						lastPantufaTimeout = System.currentTimeMillis()
					}
				} catch (e: Exception) {
					m.logger.log(Level.WARNING, e) { "Failed to get discord account info for ${discordAccount.discordId}" }
				}
			}

			val adoption = DreamCasamentos.INSTANCE.getParentsOf(player)

			if (adoption != null) {
				aboutLines.add("")
				aboutLines.add("§eParentes: §b${Bukkit.getOfflinePlayer(adoption.player1).name ?: "???"} §b${Bukkit.getOfflinePlayer(adoption.player2)?.name ?: "???"}")
			}
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
				aboutLines.joinToString("\n").toBaseComponent()
			)
		}

		textComponent += " §6➤ ".toBaseComponent()

		val split = message.split("(?=\\b[ ])")
		var previous: String? = null
		for (piece in split) {
			var editedPiece = piece
			if (previous != null) {
				editedPiece = "$previous$editedPiece"
			}
			textComponent += editedPiece.toBaseComponent()
			previous = ChatColor.getLastColors(piece)
		}

		if (DreamChat.mutedUsers.contains(player.name)) { // Usuário está silenciado
			player.spigot().sendMessage(textComponent)

			for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission("pocketdreams.soustaff")}) {
				staff.sendMessage("§8[§cSILENCIADO§8] §b${player.name}§c: $message")
			}
			return
		}

		for (onlinePlayer in Bukkit.getOnlinePlayers()) {
			// Verificar se o player está ignorando o player que enviou a mensagem
			val isIgnoringTheSender = m.userData.getStringList("ignore.${onlinePlayer.uniqueId}").contains(player.uniqueId.toString())

			if (!isIgnoringTheSender)
				onlinePlayer.spigot().sendMessage(textComponent)
		}

		val calendar = LocalDateTime.now(TimeUtils.TIME_ZONE)
		m.chatLog.appendText("[${String.format("%02d", calendar.dayOfMonth)}/${String.format("%02d", calendar.monthValue)}/${String.format("%02d", calendar.year)} ${String.format("%02d", calendar.hour)}:${String.format("%02d", calendar.minute)}] ${player.name}: $message\n")

		// Tudo OK? Então vamos verificar se a mensagem tem algo de importante para nós respondermos
		for (response in DreamChat.botResponses) {
			if (response.handleResponse(message, e)) {
				val response = response.getResponse(message, e) ?: return
				ChatUtils.sendResponseAsBot(player, response)
				break
			}
		}

		// Vamos mandar no Biscord!
		if (m.config.getBoolean("enable-chat-relay", false)) {
			val currentWebhook = DreamChat.chatWebhooks[DreamChat.currentWebhookIdx % DreamChat.chatWebhooks.size]!!
			currentWebhook.send(
				WebhookMessageBuilder()
					.setUsername(player.name)
					.setAvatarUrl("https://sparklypower.net/api/v1/render/avatar?name=${player.name}&scale=16")
					.setContent(message.stripColors()!!.replace(Regex("\\\\+@"), "@").replace("@", "@\u200B"))
					.build()
			)
			DreamChat.currentWebhookIdx++
		}
	}
}
