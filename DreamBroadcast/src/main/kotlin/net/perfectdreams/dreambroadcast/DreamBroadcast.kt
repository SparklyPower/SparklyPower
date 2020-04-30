package net.perfectdreams.dreambroadcast

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.okkero.skedule.schedule
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class DreamBroadcast : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§6➠ §e"
	}
	var lastMessageId = -1

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)

		scheduler().schedule(this) {
			while (true) {
				waitFor(20 * 90)

				var messageIdx = lastMessageId

				while (lastMessageId == messageIdx) {
					messageIdx = DreamUtils.random.nextInt(0, 18)
				}

				lastMessageId = messageIdx
				for (player in onlinePlayers()) {
					val message = TextComponent(*PREFIX.toBaseComponent())

					when (messageIdx) {
						0 -> {
							message.addExtra(
								"§eEspero que você esteja se divertindo no §4§lSparkly§b§lPower§e!".toTextComponent()
							)
						}
						1 -> {
							message.addExtra(
								"§eNão mendigue cargos no servidor, se você merecesse a vaga você já teria ela!".toTextComponent()
							)
						}
						2 -> {
							message.addExtra(
								"§eTem uma sugestão supimpa que irá melhorar o servidor §b${player.displayName}§e? Então escreva ela na §6/warp sugestões§e!".toTextComponent()
							)
						}
						3 -> {
							message.addExtra(
								if (player.girl) {
									"§eVocê é um §bgaroto§e §b${player.displayName}§e? Então use §6/garoto§e!".toTextComponent()
								} else {
									"§eVocê é uma §dgarota§e §b${player.displayName}§e? Então use §6/garota§e!".toTextComponent()
								}
							)
						}
						4 -> {
							val players = onlinePlayers().filter { it.hasPermission("perfectdreams.vip") }

							if (players.isNotEmpty()) {
								val playersName = players.joinToString("§e, §b", transform = { it.displayName })
								val prefix = if (players.size == 1) {
									if (players.first().girl) {
										"a"
									} else {
										"ao"
									}
								} else {
									"ao"
								}
								message.addExtra(
									"§eObrigado $prefix §b$playersName§e por comprar${if (players.size == 1) "" else "em"} §e§lVIP§e! §dヾ(⌐■_■)ノ♪".toTextComponent()
								)
							} else {
								message.addExtra(
									"§eQuer ajudar o §4§lSparkly§b§lPower§e §b${player.displayName}§e? Então compre §3§lVIP§be! §6/lojacash".toTextComponent()
								)
							}
						}
						5 -> {
							message.addExtra(
								"§eSe alguém estiver precisando de ajuda §b${player.displayName}§e, por favor, ajude ele. §d^-^".toTextComponent()
							)
						}
						6 -> {
							message.addExtra(
								"§eGostou do §4§lSparkly§b§lPower §b${player.displayName}§e? Então convide seus amigos! Tudo é mais divertido com amigos. §c(◕‿◕✿)".toTextComponent()
							)
						}
						7 -> {
							message.addExtra(
								"§eGostou do §4§lSparkly§b§lPower§e §b${player.displayName}§e? Então vote! Ao votar você ajuda o servidor a crescer e ainda por cima ganha prêmios! §6/votar §c(◕‿◕✿)".toTextComponent()
							)
						}
						8 -> {
							message.addExtra(
								"§eProcurando novas lojas para gastar seu dinheiro §b${player.displayName}§e? Então veja a lista de melhores lojas do servidor! §6/loja".toTextComponent()
							)
						}
						9 -> {
							message.addExtra(
								"§eEstá com sorte hoje §b${player.displayName}§e? Então aposte no cassino! §6/cassino".toTextComponent()
							)
						}
						10 -> {
							message.addExtra(
								"§eEntre no nosso servidor no Discord para conversar com outros jogadores e acompanhar notícias! §6/discord".toTextComponent()
							)
						}
						11 -> {
							message.addExtra(
								"§eCurta a nossa página no Facebook! §3§nhttps://facebook.com/SparklyPower/".toTextComponent()
							)
						}
						12 -> {
							message.addExtra(
								"§eTweet tweet, nós siga no Twitter! §3§n@SparklyPower".toTextComponent()
							)
						}
						13 -> {
							message.addExtra(
								"§eEncontrou alguma coisa errada§e §b${player.displayName}§e? Então reporte para a Staff!".toTextComponent()
							)
						}
						14 -> {
							message.addExtra(
								"§eQuer ganhar §3§lVIP§e de graça e ainda por cima ajudar o servidor§e §b${player.displayName}§e? Então vote! Você consegue pesadelos votando e, com pesadelos, você pode conseguir §3§lVIP§e! §6/votar".toTextComponent()
							)
						}
						15 -> {
							message.addExtra(
								"§eVisite a §6/lojacash§e, o lugar para gastar seus pesadelos com §3§lVIPs§e, sonhos e muito mais!".toTextComponent()
							)
						}
						16 -> {
							message.addExtra(
								"§eAdicione a Loritta no seu servidor do Discord, o maior bot brasileiro para o Discord! E, é claro, amiguinha da Pantufa :3§3§n https://loritta.website/".toTextComponent()
							)
						}
						17 -> {
							message.addExtra(
								"§eVocê já bebeu água hoje §b${player.displayName}§e? Sempre é bom se manter hidratado! §c^-^".toTextComponent()
							)
						}
						else -> {
							message.addExtra(
								"§4Parabéns, pelo visto alguém esqueceu de colocar uma mensagem para o §cidx $messageIdx§4 :c".toTextComponent()
							)
						}
					}

					player.spigot().sendMessage(message)
				}
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onSocket(e: SocketReceivedEvent) {
		val type = e.json["type"].nullString ?: return

		if (type == "news_posted") {
			val author = e.json["author"].string
			val title = e.json["title"].string
			val link = e.json["link"].string

			val message = TextComponent(*PREFIX.toBaseComponent())
			message.addExtra(
				"§b$author§e publicou uma nova postagem! \"§9$title§e\" §8-§e Leia sobre aqui! §8»§e ".toTextComponent()
			)
			message.addExtra(
				"§3§n$link".toTextComponent().apply {
					clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, link)
				}
			)

			val messageAfter = TextComponent(*PREFIX.toBaseComponent())
			messageAfter.addExtra(
				"§eVeja a postagem mais recente sobre o PerfectDreams! \"§9$title§e\" §8-§e Leia sobre aqui! §8»§e ".toTextComponent()
			)
			messageAfter.addExtra(
				"§3§n$link".toTextComponent().apply {
					clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, link)
				}
			)

			scheduler().schedule(this) {
				Bukkit.broadcast(message)
				for (idx in 0..11) {
					waitFor(6000)
					Bukkit.broadcast(messageAfter)
				}
			}
		}
	}
}