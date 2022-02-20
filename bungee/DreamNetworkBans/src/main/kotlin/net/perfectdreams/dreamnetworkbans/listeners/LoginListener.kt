package net.perfectdreams.dreamnetworkbans.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamcorebungee.network.DreamNetwork
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.extensions.toBaseComponent
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.Ban
import net.perfectdreams.dreamnetworkbans.dao.Fingerprint
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.dao.IpBan
import net.perfectdreams.dreamnetworkbans.tables.*
import net.perfectdreams.dreamnetworkbans.utils.DateUtils
import net.perfectdreams.dreamnetworkbans.utils.GeoUtils
import net.perfectdreams.dreamnetworkbans.utils.LoginConnectionStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.Field
import java.util.*
import java.util.logging.Level
import java.util.regex.Pattern
import kotlin.collections.HashMap

class LoginListener(val m: DreamNetworkBans) : Listener {
	val isOnlineModeSet = HashMap<String, Boolean>()
	val nameToPremiumData = HashMap<String, ResultRow>()
	val pingedByAddresses = Collections.synchronizedSet(mutableSetOf<String>())

	private fun isGeyser(connection: PendingConnection): Boolean {
		// To detect and keep player IPs correctly, we use a separate Bungee listener that uses the PROXY protocol
		// To check if the user is connecting thru Geyser, we will check if the MOTD matches what we would expect
		return connection.listener.motd == DreamNetworkBans.GEYSER_LISTENER_MOTD
	}

	@EventHandler
	fun onPing(event: ProxyPingEvent) {
		pingedByAddresses.add(event.connection.address.hostString)
	}

	@EventHandler
	fun onPreLogin(event: PreLoginEvent) {
		val isGeyser = isGeyser(event.connection)

		m.logger.info { "User ${event.connection.name} is pre logging in... Is Geyser? $isGeyser" }
		val playerNames = m.proxy.players.map { it.name.toLowerCase() }
		if (event.connection.name.toLowerCase() in playerNames) {
			event.isCancelled = true
			event.setCancelReason("§cJá há um player com o nome §e${event.connection.name}§c conectado no servidor!".toTextComponent())
			return
		}

		val onlinePlayersWithSpecificIp = m.proxy.players.count {
			event.connection.address.address.hostAddress == it.address.address.hostAddress
		}

		m.logger.info { "Count of players with IP ${event.connection.address.address.hostAddress} connected to the server: $onlinePlayersWithSpecificIp" }
		if (onlinePlayersWithSpecificIp >= 5) { // Se for maior que três...
			// get dunked!!
			event.isCancelled = true
			event.setCancelReason(*"§cJá existem muitas contas logadas com o mesmo IP no servidor!".toBaseComponent())
			return
		}

		// mcdrop.io
		if (event.connection.name.startsWith("mcdrop_", true)) {
			// get dunked²
			event.isCancelled = true
			event.setCancelReason(*"§cJá existem muitas contas logadas com o mesmo IP no servidor!".toBaseComponent())
			return
		}

		// We won't check for Geyser here due to MOTD pings being unreliable
		if (!isGeyser && !pingedByAddresses.contains(event.connection.address.hostString)) {
			event.isCancelled = true
			event.setCancelReason(*"§cAdicione §emc.sparklypower.net§c na sua lista de servidores antes de começar a jogar! ^-^\n\nSim, é muito tosco fazer isso, mas serve como uma verificação ;w;".toBaseComponent())
			return
		}

		val staffIps = DreamUtils.jsonParser.parse(m.staffIps.readText(Charsets.UTF_8)).obj
		staffIps.entrySet().forEach {
			if (it.key.equals(event.connection.name, ignoreCase = true) && event.connection.virtualHost.hostString != it.value.string) {
				event.registerIntent(m)

				m.proxy.scheduler.runAsync(m) {
					event.isCancelled = true

					val alreadyBanned = transaction(Databases.databaseNetwork) {
						IpBans.select {
							IpBans.ip eq event.connection.address.hostString
						}.count() != 0L
					}

					if (!alreadyBanned)
						m.proxy.pluginManager.dispatchCommand(m.proxy.console, "ipban ${event.connection.address.hostString} Tentar entrar com uma conta de um membro da equipe do SparklyPower. Tenha mais sorte na próxima vez! Porque pelo visto você falhou ^-^")

					// Trollei
					event.setCancelReason("Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host".toTextComponent())
					// addToConnectionLog(event, LoginConnectionStatus.USING_STAFF_NAME)
					event.completeIntent(m)
					return@runAsync
				}
			}
		}

		event.registerIntent(m)

		m.proxy.scheduler.runAsync(m) {
			// Don't check if the user is premium if they are using Geyser
			if (DreamNetworkBans.bypassPremiumCheck || isGeyser) {
				m.logger.info("Bypassing Premium Check for ${event.connection}")
				event.completeIntent(m)
				return@runAsync
			}

			val playerUniqueIdResult = runBlocking {
				runCatching {
					m.minecraftMojangApi.getUniqueId(event.connection.name)
				}
			}

			if (playerUniqueIdResult.isFailure) {
				val exception = playerUniqueIdResult.exceptionOrNull()
				// Something went wrong while querying the UUID!
				m.logger.log(Level.WARNING, exception) { "Something went wrong while querying ${event.connection.name}'s UUID!" }

				event.isCancelled = true
				event.setCancelReason(*"§cParece que a Mojang está tendo dificuldades no momento, tente mais tarde! :(".toBaseComponent())

				event.completeIntent(m)
				return@runAsync
			}


			val playerUniqueId = playerUniqueIdResult.getOrThrow() // Should never throw

			m.logger.info("Player ${event.connection} premium UUID is $playerUniqueId")

			if (playerUniqueId == null) { // Se o UUID for nulo, então é cracked *ou* estamos tomando rate limit
				// But there is a catch! We also need to check if the user matches the cracked UUID!
				val crackedUniqueId = UUID.nameUUIDFromBytes("OfflinePlayer:${event.connection.name}".toByteArray(Charsets.UTF_8))

				val premiumStatus = transaction(Databases.databaseNetwork) {
					PremiumUsers.select {
						PremiumUsers.crackedUniqueId eq crackedUniqueId
					}.firstOrNull()
				}

				if (premiumStatus != null) {
					// Hell no, get out!
					event.isCancelled = true
					event.setCancelReason(*"§cVocê precisa entrar com a sua conta de Minecraft original!".toBaseComponent())
				}

				event.completeIntent(m)
				return@runAsync
			}

			// Parece que o player na Mojang realmente existe!
			// Vamos verificar se é um premium player...
			val premiumStatus = transaction(Databases.databaseNetwork) {
				PremiumUsers.select {
					PremiumUsers.premiumUniqueId eq playerUniqueId
				}.firstOrNull()
			}

			if (premiumStatus != null) {
				m.logger.info("Premium Status is set for ${event.connection}! Online Mode is on!!")
				// Realmente é um player premium, wow!
				// Vamos alterar o player para online mode então! :gesso:
				event.connection.isOnlineMode = true
				isOnlineModeSet[event.connection.name] = true
				nameToPremiumData[event.connection.name] = premiumStatus
			} else {
				m.logger.info("Premium Status is not set for ${event.connection} so we are going to set it to off...")
			}

			event.completeIntent(m)
			return@runAsync
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onLogin(event: LoginEvent) {
		val isGeyser = isGeyser(event.connection)
		idField.set(event.connection, UUID.nameUUIDFromBytes("OfflinePlayer:${event.connection.name}".toByteArray(Charsets.UTF_8)))

		// If the player has spaces in the username, remove them
		// This would only happen on a Geyser connection or a pirated MC connection, but we will only replace if it is Geyser
		if (isGeyser && event.connection.name.contains(" ")) { // Geyser
			val newName = event.connection.name.replace(" ", "_")
			idField.set(event.connection, UUID.nameUUIDFromBytes("OfflinePlayer:$newName".toByteArray(Charsets.UTF_8)))
			val nameField: Field = InitialHandler::class.java.getDeclaredField("name")
			nameField.isAccessible = true
			nameField.set(event.connection, newName)
		}

		val premiumData = nameToPremiumData[event.connection.name]
		if (premiumData != null) {
			idField.set(event.connection, premiumData[PremiumUsers.crackedUniqueId])

			// No need to store that anymore, thank you xoxo
			nameToPremiumData.remove(event.connection.name)

			m.logger.info("Player ${event.connection} was successfully logged in as a premium user!")
		}

		event.registerIntent(m)

		m.proxy.scheduler.runAsync(m) {
			val pattern = Pattern.compile("[a-zA-Z0-9_]{3,16}")
			val matcher = pattern.matcher(event.connection.name)

			if (!matcher.matches()) {
				event.isCancelled = true
				event.setCancelReason(*"""
				§cSeu nickname não atende aos critérios necessários!

				§cProvavelmente:
				§c - Seu nickname contém caracteres especiais (como $, #, @, %, etc)
				§c - Seu nickname contém espaços
				§c - Seu nickname contém mais de 16 caracteres
				§c - Seu nickname contém menos de 3 caracteres
			""".trimIndent().toBaseComponent())
				addToConnectionLog(event, LoginConnectionStatus.INVALID_NAME)
				event.completeIntent(m)
				return@runAsync
			}

			if (event.connection.name.toLowerCase() in m.youtuberNames) {
				event.isCancelled = true
				event.setCancelReason(*"""
				§eVocê parece ser alguém famoso...
					|
					|§aCaso você seja §b${event.connection.name}§a, por favor, mande um email confirmando a sua identidade para §3mrpowergamerbr@perfectdreams.net§a, obrigado! :)
					|
					|§aSei que é chato, mas sempre existem aquelas pessoas mal intencionadas que tentam se passar por YouTubers... :(
					""".trimMargin().toBaseComponent())
				addToConnectionLog(event, LoginConnectionStatus.USING_YOUTUBER_NAME)
				event.completeIntent(m)
				return@runAsync
			}

			// Player logou, então vamos causar:tm:
			if (isOnlineModeSet.containsKey(event.connection.name)) { // O player entrou e está marcado como online mode!
				if (premiumData != null) {
					// Time to notify the lobby that we don't need to authenticate the current user because he is a premium user!
					m.logger.info("Player ${event.connection} is going to be notified as a premium user...")

					// E avisar para o auth server que é um player premium
					DreamNetwork.PERFECTDREAMS_LOBBY.send(
						jsonObject(
							"type" to "addToPremiumPlayerList",
							"uniqueId" to premiumData[PremiumUsers.crackedUniqueId].toString()
						)
					)
				} else {
					m.logger.info("Player ${event.connection} logged in as a premium user, but doesn't have premium status...?")
					event.isCancelled = true
					event.setCancelReason(*"""§cConta logou em Online Mode, mas premium status não existe. (bug?)""".trimMargin().toBaseComponent())
					event.completeIntent(m)
					return@runAsync
				}
			} else {
				m.logger.info("Player ${event.connection} logged in as a cracked user!")
			}

			isOnlineModeSet.remove(event.connection.name)

			val uniqueId = event.connection.uniqueId
			val playerName = event.connection.name

			if (isGeyser)
				DreamNetwork.PERFECTDREAMS_SURVIVAL.send(
					jsonObject(
						"type" to "addToGeyserPlayerList",
						"uniqueId" to uniqueId.toString()
					)
				)

			// We need to update it on the DreamNetworkBans plugin because BungeeCord does *not* respect event priority.
			// This sucks, but sadly that's what we need to do :(
			transaction(Databases.databaseNetwork) {
				val user = User.findById(uniqueId) ?: User.new(uniqueId) {
					m.logger.info { "Creating new user data for ($playerName / $uniqueId)" }
					this.username = playerName
				}

				m.logger.info { "Updated (${user.username} / ${user.id}) with ($playerName / $uniqueId) data" }

				user.username = playerName
			}

			val playerIp = event.connection.address.hostString

			// Check if the ASN is blacklisted
			val asnBlacklisted = m.asnManager.isAsnBlacklisted(playerIp)
			m.logger.info { "ASN check result for $playerIp: $asnBlacklisted; Quantity of triggered ASNs: ${m.asnManager.triggeredAsns.size}/${m.asnManager.asns.size}" }

			if (asnBlacklisted) {
				event.isCancelled = true
				event.setCancelReason(*"""
							§cSeu IP está bloqueado, desative VPNs ou proxies ativos para poder jogar!"
						""".trimIndent().toBaseComponent())
				addToConnectionLog(event, LoginConnectionStatus.BLACKLISTED_ASN)
				event.completeIntent(m)
				return@runAsync
			}

			val geoLocalization = transaction(Databases.databaseNetwork) {
				GeoLocalization.find { GeoLocalizations.ip eq playerIp }.firstOrNull()
			}

			if (geoLocalization == null || System.currentTimeMillis() >= geoLocalization.retrievedAt + (1_000 * 60 * 60 * 24)) {
				// Apenas iremos verificar o GeoLocation se já se passou mais de um dia desde a última verificação ou se a geolocalização é nula
				// Vamos executar isto em uma thread externa, para evitar problemas
				m.proxy.scheduler.runAsync(m) {
					val loc = GeoUtils.getGeolocalization(playerIp)

					transaction(Databases.databaseNetwork) {
						GeoLocalization.new {
							this.ip = event.connection.address.hostString

							this.country = loc.country
							this.region = loc.regionName
							this.retrievedAt = System.currentTimeMillis()
						}
					}
				}
			}

			val ban = transaction(Databases.databaseNetwork) {
				Ban.find { Bans.player eq event.connection.uniqueId }.firstOrNull()
			}

			if (ban != null) {
				if (!ban.temporary || ban.expiresAt!! >= System.currentTimeMillis()) {
					event.isCancelled = true

					event.setCancelReason(*"""
							§cVocê foi ${if (ban.temporary) "temporariamente " else ""}banido!
							§cMotivo:
							
							§a${ban.reason}
							§cPor: ${PunishmentManager.getPunisherName(ban.punishedBy)}
							${if (ban.temporary) "§c Expira em: §e${DateUtils.formatDateDiff(ban.expiresAt!!)}" else
						"§3Quer tentar uma segunda chance? Acesse o link, leia e preencha com atenção: §6bit.ly/sparklypower-unban"}
						""".trimIndent().toBaseComponent())
					addToConnectionLog(event, LoginConnectionStatus.BANNED)
					event.completeIntent(m)
					return@runAsync
				}
			}

			val ipBan = transaction(Databases.databaseNetwork) {
				IpBan.find { IpBans.ip eq event.connection.address.hostString }.firstOrNull()
			}

			// Because MCPE connections via Geyser uses "127.0.0.1" (for now), we will just ignore IP bans if they are bound to "127.0.0.1"
			if (ipBan != null && ipBan.ip != "127.0.0.1") {
				if (!ipBan.temporary || ipBan.expiresAt!! >= System.currentTimeMillis()) {
					event.isCancelled = true
					event.setCancelReason(*"""
							§cVocê foi ${if (ipBan.temporary) "temporariamente " else ""}banido!
							§cMotivo:
							
							§a${ipBan.reason}
							§cPor: ${PunishmentManager.getPunisherName(ipBan.punishedBy)}
							${if (ipBan.temporary) "§c Expira em: §e${DateUtils.formatDateDiff(ipBan.expiresAt!!)}" else
						"§3Quer tentar uma segunda chance? Acesse o link, leia e preencha com atenção: §6bit.ly/sparklypower-unban"}
						""".trimIndent().toBaseComponent())
					addToConnectionLog(event, LoginConnectionStatus.IP_BANNED)
					event.completeIntent(m)
					return@runAsync
				}
			}

			// THIS IS FOR DEBUGGING/DEV!!
			if (!m.requireDreamAuthLogin)
				m.loggedInPlayers.add(event.connection.uniqueId)

			addToConnectionLog(event, LoginConnectionStatus.OK)
			event.completeIntent(m)
		}
	}

	@EventHandler
	fun onDisconnect(event: ServerKickEvent) {
		// TODO: Remover "event.kickReason", já que está deprecated :whatdog:
		if (event.kickReason.contains("Server closed", true) && event.kickedFrom.name != "sparklypower_lobby") {
			event.isCancelled = true
			event.cancelServer = m.proxy.getServerInfo("sparklypower_lobby")
		}
	}

	@EventHandler
	fun onQuit(event: PlayerDisconnectEvent) {
		DreamNetwork.PERFECTDREAMS_LOBBY.send(
			jsonObject(
				"type" to "removeFromPremiumPlayerList",
				"uniqueId" to event.player.uniqueId.toString()
			)
		)

		DreamNetwork.PERFECTDREAMS_SURVIVAL.send(
			jsonObject(
				"type" to "removeFromGeyserPlayerList",
				"uniqueId" to event.player.uniqueId.toString()
			)
		)
	}

	fun addToConnectionLog(event: LoginEvent, state: LoginConnectionStatus) {
		transaction(Databases.databaseNetwork) {
			ConnectionLogEntries.insert {
				it[player] = event.connection.uniqueId
				it[ip] = event.connection.address.hostString
				it[connectedAt] = System.currentTimeMillis()
				it[connectionStatus] = state
			}
		}
	}

	@EventHandler
	fun onSettingsChange(event: SettingsChangedEvent) {
		m.logger.info { "Player ${event.player.name} changed its settings!" }

		transaction(Databases.databaseNetwork) {
			Fingerprint.new {
				this.player = event.player.uniqueId
				this.createdAt = System.currentTimeMillis()
				this.isForgeUser = event.player.isForgeUser
				this.chatMode = event.player.chatMode
				this.mainHand = event.player.mainHand
				this.language = event.player.locale.language
				this.viewDistance = event.player.viewDistance.toInt()
				this.version = event.player.pendingConnection.version.toString() // ProtocolSupportAPI.getProtocolVersion(event.player).name

				this.hasCape = event.player.skinParts.hasCape()
				this.hasHat = event.player.skinParts.hasHat()
				this.hasJacket = event.player.skinParts.hasJacket()
				this.hasLeftPants = event.player.skinParts.hasLeftPants()
				this.hasLeftSleeve = event.player.skinParts.hasLeftSleeve()
				this.hasRightPants = event.player.skinParts.hasRightPants()
				this.hasRightSleeve = event.player.skinParts.hasRightSleeve()
			}
		}
	}

	companion object {
		val idField: Field = InitialHandler::class.java.getDeclaredField("uniqueId").apply {
			isAccessible = true
		}
	}
}