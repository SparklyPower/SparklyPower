package net.sparklypower.sparklyneonvelocity.utils.socket

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.utils.DreamUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SocketServer(val m: SparklyNeonVelocity, val server: ProxyServer, val socketPort: Int) {
	fun start() {
		val listener = ServerSocket(socketPort, 0, null)
		try {
			while (true) {
				val socket = listener.accept()
				GlobalScope.launch {
					try {
						val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
						val reply = fromClient.readLine()
						val jsonObject = JsonParser.parseString(reply).obj
						var response = JsonObject()

						val type = jsonObject["type"].nullString ?: return@launch

						when (type) {
							"getOnlinePlayers" -> {
								val players = server.allPlayers.map { it.username }
								response["players"] = DreamUtils.gson.toJsonTree(players)
								response["api:code"] = 0
							}
							"getOnlinePlayersInfo" -> {
								val servers = jsonArray()
								server.allServers.forEach { info ->
									val server = jsonObject()
									server["name"] = info.serverInfo.name
									val players = info.playersConnected.map {
										jsonObject(
											"name" to it.username,
											"server" to it.currentServer.getOrNull()?.server?.serverInfo?.name,
											"locale" to (it.effectiveLocale?.toString() ?: "???"),
											"ping" to it.ping,
											"isForgeUser" to false
										)
									}
									server["players"] = DreamUtils.gson.toJsonTree(players)
									servers.add(server)
								}
								response["servers"] = servers
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
								val playerName = jsonObject["player"].string
								val bungeeServer = jsonObject["bungeeServer"].string
								val serverIp = jsonObject["serverIP"].nullString
								val serverPort = jsonObject["serverPort"].nullInt

								val serverInfo = if (serverIp != null && serverPort != null) {
									TODO("You cannot move to a non-registered server!")
								} else {
									server.getServer(bungeeServer).get()
								}

								val player = server.getPlayer(playerName).getOrNull()

								if (player == null) {
									response = SocketUtils.createErrorPayload(
										SocketCode.UNKNOWN_PLAYER,
										"Player ${playerName} não existe ou está offline!"
									)
								} else {
									if (serverInfo == null) {
										response = SocketUtils.createErrorPayload(
											SocketCode.UNKNOWN_SERVER,
											"Servidor não existe!"
										)
									} else {
										player.createConnectionRequest(serverInfo).fireAndForget()
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
								val playerNames = jsonObject["players"].array
								val bungeeServer = jsonObject["bungeeServer"].string
								val serverIp = jsonObject["serverIP"].nullString
								val serverPort = jsonObject["serverPort"].nullInt

								val serverInfo = if (serverIp != null && serverPort != null) {
									TODO("You cannot move to a non-registered server!")
								} else {
									server.getServer(bungeeServer).get()
								}

								val players = playerNames.mapNotNull { server.getPlayer(it.string).getOrNull() }

								if (serverInfo == null) {
									response = SocketUtils.createErrorPayload(
										SocketCode.UNKNOWN_SERVER,
										"Servidor não existe!"
									)
								} else {
									players.forEach { it.createConnectionRequest(serverInfo).fireAndForget() }

									if (players.size == playerNames.size()) {
									} else {
										// Pegar jogadores que não estão online
										val notOnlinePlayers = playerNames.map {
											it.string
										}.filter {
											!server.getPlayer(it).isPresent()
										}

										response["failedToTransfer"] = DreamUtils.gson.toJsonTree(notOnlinePlayers)
									}
								}
							}
							"loggedIn" -> {
								val player = jsonObject["player"].string
								m.loggedInPlayers.add(UUID.fromString(player))
								m.logger.info("Player ${player} foi marcado como logado na rede! Yay!")
							}
							"sendAdminChat" -> {
								val user = jsonObject["player"].string
								val message = jsonObject["message"].string.ifBlank { "*mensagem vazia*" }
								val isLargeMessage = with (message) { length > 256 || count { it == '\n' } >= 5 }

								val key = "/${UUID.randomUUID()}"
								val text = "\uE23C §x§9§2§A§9§F§4$user: §x§C§8§D§3§F§4$message"

								server.allPlayers
									.filter { it.hasPermission("sparklyneonvelocity.adminchat") && it.uniqueId in m.loggedInPlayers }
									.forEach {
										it.sendMessage(text.fromLegacySectionToTextComponent())
									}
							}
							"executeCommand" -> {
								val player = jsonObject["player"].string
								val command = jsonObject["command"].string

								val commandSender = FakeCommandSender(player)

								m.logger.info { "Dispatching command $command by $player!" }

								server.commandManager.executeAsync(commandSender, command).get()

								m.logger.info { "Command Output is ${commandSender.output}" }
								response["messages"] = commandSender.output.toJsonArray()
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
								val playerNames = jsonObject["players"].array
								val bungeeServer = jsonObject["bungeeServer"].string
								val serverIp = jsonObject["serverIP"].nullString
								val serverPort = jsonObject["serverPort"].nullInt

								val serverInfo = if (serverIp != null && serverPort != null) {
									TODO("You cannot move to a non-registered server!")
								} else {
									server.getServer(bungeeServer).get()
								}

								val players = playerNames.mapNotNull { server.getPlayer(UUID.fromString(it.string)).getOrNull() }

								if (serverInfo == null) {
									response = SocketUtils.createErrorPayload(
										SocketCode.UNKNOWN_SERVER,
										"Servidor não existe!"
									)
								} else {
									players.forEach { it.createConnectionRequest(serverInfo).fireAndForget() }

									if (players.size != playerNames.size()) {
										// Pegar jogadores que não estão online
										val notOnlinePlayers = playerNames.map {
											it.string
										}.filter {
											server.getPlayer(it) == null
										}

										response["failedToTransfer"] = DreamUtils.gson.toJsonTree(notOnlinePlayers)
									}
								}
							}
						}

						val out = PrintWriter(socket.getOutputStream(), true)
						out.println(response.toString() + "\n")
						out.flush()
						fromClient.close()
					} finally {
						socket.close()
					}
				}
			}
		} finally {
			listener.close()
		}
	}

	class FakeCommandSender(val playerName: String) : CommandSource {
		val output = mutableListOf<String>()

		override fun getPermissionValue(permission: String) = Tristate.TRUE

		override fun sendMessage(source: Identity, message: Component, type: MessageType) {
			output.add(PlainTextComponentSerializer.plainText().serialize(message))
		}
	}
}