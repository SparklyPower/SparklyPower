package net.perfectdreams.dreamcorebungee.listeners

import com.github.salomonbrys.kotson.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamcorebungee.DreamCoreBungee
import net.perfectdreams.dreamcorebungee.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcorebungee.network.socket.SocketUtils
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.SocketCode
import java.net.InetSocketAddress

class SocketListener : Listener {
	@EventHandler
	fun onSocket(e: SocketReceivedEvent) {
		val proxy = DreamCoreBungee.INSTANCE.proxy
		val type = e.json["type"].nullString ?: return

		when (type) {
			"getOnlinePlayers" -> {
				val players = proxy.players.map { it.name }
				e.response["players"] = DreamUtils.gson.toJsonTree(players)
				e.response["api:code"] = 0
			}
			"getOnlinePlayersInfo" -> {
				val servers = jsonArray()
				proxy.serversCopy.forEach { name, info ->
					val server = jsonObject()
					server["name"] = name
					val players = info.players.map {
						jsonObject(
								"name" to it.name,
								"server" to it.server.info.name,
								"locale" to it.locale.toString(),
								"ping" to it.ping,
								"isForgeUser" to it.isForgeUser
						)
					}
					server["players"] = DreamUtils.gson.toJsonTree(players)
					servers.add(server)
				}
				e.response["servers"] = servers
			}
			"transferPlayer" -> {
				// Transfere um player para outro servidor
				// player: nome do jogador
				// bungeeServer: nome do servidor BungeeCord
				// serverIP: IP do servidor (opcional)
				// serverPort: Porta do servidor (opcional)
				// ===[ API CODES ]===
				// 0: Sucesso
				// 1: Player indisponível
				// 2: Servidor inexistente
				val playerName = e.json["player"].string
				val bungeeServer = e.json["bungeeServer"].string
				val serverIp = e.json["serverIP"].nullString
				val serverPort = e.json["serverPort"].nullInt

				val serverInfo = if (serverIp != null && serverPort != null) {
					proxy.constructServerInfo(bungeeServer, InetSocketAddress(serverIp, serverPort), "", false)
				} else {
					proxy.getServerInfo(bungeeServer)
				}

				val player = proxy.getPlayer(playerName)

				if (player == null) {
					e.response = SocketUtils.createErrorPayload(
							SocketCode.UNKNOWN_PLAYER,
							"Player ${playerName} não existe ou está offline!"
					)
					return
				} else {
					if (serverInfo == null) {
						e.response = SocketUtils.createErrorPayload(
								SocketCode.UNKNOWN_SERVER,
								"Servidor não existe!"
						)
						return
					} else {
						player.connect(serverInfo)
					}
				}
			}
			"transferPlayers" -> {
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
					proxy.constructServerInfo(bungeeServer, InetSocketAddress(serverIp, serverPort), "", false)
				} else {
					proxy.getServerInfo(bungeeServer)
				}

				val players = playerNames.mapNotNull { proxy.getPlayer(it.string) }

				if (serverInfo == null) {
					e.response = SocketUtils.createErrorPayload(
							SocketCode.UNKNOWN_SERVER,
							"Servidor não existe!"
					)
					return
				} else {
					players.forEach { it.connect(serverInfo) }

					if (players.size == playerNames.size()) {
					} else {
						// Pegar jogadores que não estão online
						val notOnlinePlayers = playerNames.map {
							it.string
						}.filter {
							proxy.getPlayer(it) == null
						}

						e.response["failedToTransfer"] = DreamUtils.gson.toJsonTree(notOnlinePlayers)
					}
				}
			}
		}
	}
}