package net.perfectdreams.dreamnetworkbans.listeners

import com.github.salomonbrys.kotson.*
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamcorebungee.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcorebungee.network.socket.SocketUtils
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.SocketCode
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import java.net.InetSocketAddress
import java.util.*

class SocketListener(val m: DreamNetworkBans) : Listener {
	companion object {
		val commands = listOf(
				"/login",
				"/logar",
				"/registrar",
				"/register"
		)
	}

	@EventHandler
	fun onQuit(e: PlayerDisconnectEvent) {
		m.loggedInPlayers.remove(e.player.uniqueId)
	}

	@EventHandler
	fun onCommand(e: ChatEvent) {
		val sender = e.sender

		if (e.isCommand && sender is ProxiedPlayer) { // Cancelar coisas que não podem ser executadas antes de logar
			if (m.loggedInPlayers.contains(sender.uniqueId))
				return

			if (commands.any { e.message.startsWith(it, true) })
				return

			e.isCancelled = true
		}
	}

	@EventHandler
	fun onSocketReceived(e: SocketReceivedEvent) {
		val type = e.json["type"].nullString ?: return

		// Exemplo de JSON...
		// { "type": "ping" }

		// Caso aconteça algum erro, { "error": { ... } }
		when (type) {
			"loggedIn" -> {
				val player = e.json["player"].string
				m.loggedInPlayers.add(UUID.fromString(player))
				m.logger.info("Player ${player} foi marcado como logado na rede! Yay!")
			}
			"sendAdminChat" -> {
				val player = e.json["player"].string
				val message = e.json["message"].string

				val staff = m.proxy.players.filter { it.hasPermission("dreamnetworkbans.adminchat") }

				val senderName = player

				val color = "§d"

				val tc = "§3[$color(Discord) §l${senderName}§3] §b$message".toTextComponent()

				staff.forEach { it.sendMessage(tc) }
			}
			"executeCommand" -> {
				val player = e.json["player"].nullString
				val command = e.json["command"].string

				val commandSender = if (player != null) {
					FakeCommandSender(player)
				} else {
					m.proxy.console
				}

				m.logger.info { "Dispatching command $command by $player!" }

				m.proxy.pluginManager.dispatchCommand(commandSender, command)

				if (commandSender is FakeCommandSender) {
					e.response["messages"] = commandSender.output.toJsonArray()
				}
			}
			"transferPlayersByUUID" -> {
				// Transfere vários players para outro servidor
				// players: nomes de jogadores
				// bungeeServer: nome do servidor BungeeCord
				// serverIP: IP do servidor (opcional)
				// serverPort: Porta do servidor (opcional)
				// ===[ API CODES ]===
				// 0: Sucesso
				// 1: Sucesso, mas alguns players não puderam ser transferidos
				// 2: Servidor inexistente
				val playerNames = e.json["players"].array
				val bungeeServer = e.json["bungeeServer"].string
				val serverIp = e.json["serverIP"].nullString
				val serverPort = e.json["serverPort"].nullInt

				val serverInfo = if (serverIp != null && serverPort != null) {
					m.proxy.constructServerInfo(bungeeServer, InetSocketAddress(serverIp, serverPort), "", false)
				} else {
					m.proxy.getServerInfo(bungeeServer)
				}

				val players = playerNames.mapNotNull { m.proxy.getPlayer(UUID.fromString(it.string)) }

				if (serverInfo == null) {
					e.response = SocketUtils.createErrorPayload(
							SocketCode.UNKNOWN_SERVER,
							"Servidor não existe!"
					)
					return
				} else {
					players.forEach { it.connect(serverInfo) }

					if (players.size != playerNames.size()) {
						// Pegar jogadores que não estão online
						val notOnlinePlayers = playerNames.map {
							it.string
						}.filter {
							m.proxy.getPlayer(it) == null
						}

						e.response["failedToTransfer"] = DreamUtils.gson.toJsonTree(notOnlinePlayers)
					}
				}
			}
		}
	}

	class FakeCommandSender(val playerName: String) : CommandSender {
		val output = mutableListOf<String>()

		override fun sendMessage(p0: String) {
			output.add(p0)
		}

		override fun sendMessage(vararg p0: BaseComponent?) {
			p0.filterNotNull().forEach {
				output.add(it.toPlainText())
			}
		}

		override fun sendMessage(p0: BaseComponent?) {
			if (p0 == null)
				return

			output.add(p0.toPlainText())
		}

		override fun addGroups(vararg p0: String?) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun setPermission(p0: String?, p1: Boolean) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getName(): String {
			return playerName
		}

		override fun removeGroups(vararg p0: String?) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun sendMessages(vararg p0: String?) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getGroups(): MutableCollection<String> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getPermissions(): MutableCollection<String> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun hasPermission(p0: String?): Boolean {
			return true
		}
	}
}
