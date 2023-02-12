package net.perfectdreams.dreamcorebungee.network

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import net.perfectdreams.dreamcorebungee.network.socket.SocketUtils
import net.perfectdreams.dreamcorebungee.utils.Constants.LOCAL_HOST
import net.perfectdreams.dreamcorebungee.utils.Constants.LORITTA_PORT
import net.perfectdreams.dreamcorebungee.utils.Constants.PANTUFA_PORT
import net.perfectdreams.dreamcorebungee.utils.Constants.PERFECTDREAMS_BUNGEE_PORT
import net.perfectdreams.dreamcorebungee.utils.Constants.PERFECTDREAMS_LOBBY_PORT
import net.perfectdreams.dreamcorebungee.utils.Constants.PERFECTDREAMS_SURVIVAL_PORT

open class DreamNetwork {
	companion object {
		val LORITTA = DreamServer(LOCAL_HOST, LORITTA_PORT, "loritta", "Loritta", "Loritta")
		val PANTUFA = PantufaServer(LOCAL_HOST, PANTUFA_PORT, "pantufa", "Pantufa", "Pantufa")
		val PERFECTDREAMS_BUNGEE = MinecraftServer("sparkly-bungee", 60800, "bungeecord", "PerfectDreams BungeeCord", "BungeeCord")
		val PERFECTDREAMS_LOBBY = MinecraftServer("sparkly-lobby", 60800, "sparklypower_lobby", "PerfectDreams Lobby", "Lobby")
		val PERFECTDREAMS_SURVIVAL = MinecraftServer("sparkly-survival", 60800, "sparklypower_survival", "PerfectDreams Lobby", "Lobby")

		val servers = mutableListOf<DreamServer>()

		init {
			servers.add(LORITTA)
			servers.add(PANTUFA)
			servers.add(PERFECTDREAMS_BUNGEE)
			servers.add(PERFECTDREAMS_LOBBY)
		}

		fun getByInternalName(internalName: String) = servers.firstOrNull { it.internalName == internalName }
	}

	open class DreamServer(val host: String, val socketPort: Int, val internalName: String, val fancyName: String, val name: String) {
		fun sendAsync(jsonObject: JsonObject, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) = SocketUtils.sendAsync(jsonObject, host, socketPort, success, error)
		fun send(jsonObject: JsonObject) = SocketUtils.send(jsonObject, host, socketPort)
	}

	class MinecraftServer(host: String, socketPort: Int, internalName: String, fancyName: String, name: String) : DreamServer(host, socketPort, internalName, fancyName, name)

	class PantufaServer(host: String, socketPort: Int, internalName: String, fancyName: String, name: String) : DreamServer(host, socketPort, internalName, fancyName, name) {
		fun sendMessageAsync(channelId: String, message: String, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) {
			return sendAsync(
					jsonObject(
							"type" to "sendMessage",
							"textChannelId" to channelId,
							"message" to message
					),
					success,
					error
			)
		}

		fun sendMessage(channelId: String, message: String): JsonObject {
			return send(
					jsonObject(
							"type" to "sendMessage",
							"textChannelId" to channelId,
							"message" to message
					)
			)
		}
	}
}