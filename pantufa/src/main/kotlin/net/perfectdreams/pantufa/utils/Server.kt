package net.perfectdreams.pantufa.utils

import com.google.gson.JsonObject
import net.perfectdreams.pantufa.utils.Constants.LOCAL_HOST
import net.perfectdreams.pantufa.utils.Constants.LORITTA_PORT
import net.perfectdreams.pantufa.utils.Constants.PERFECTDREAMS_BUNGEE_PORT
import net.perfectdreams.pantufa.utils.Constants.PERFECTDREAMS_LOBBY_PORT
import net.perfectdreams.pantufa.utils.Constants.PERFECTDREAMS_SURVIVAL_PORT
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import java.net.ConnectException

class Server(val host: String, val socketPort: Int, val internalName: String, val fancyName: String, val name: String) {
	companion object {
		val LORITTA = Server(LOCAL_HOST, LORITTA_PORT, "loritta", "Loritta", "Loritta")
		val PERFECTDREAMS_BUNGEE = Server("10.5.0.2", PERFECTDREAMS_BUNGEE_PORT, "bungeecord", "SparklyPower BungeeCord", "BungeeCord")
		val PERFECTDREAMS_LOBBY = Server("10.5.0.3", PERFECTDREAMS_LOBBY_PORT, "sparklypower_lobby", "SparklyPower Lobby", "Lobby")
		val PERFECTDREAMS_SURVIVAL = Server("10.5.0.4", PERFECTDREAMS_SURVIVAL_PORT, "sparklypower_survival", "SparklyPower Survival", "Survival")
		val servers = mutableListOf<Server>()

		init {
			servers.add(LORITTA)
			servers.add(PERFECTDREAMS_BUNGEE)
			servers.add(PERFECTDREAMS_LOBBY)
			servers.add(PERFECTDREAMS_SURVIVAL)
		}

		fun getByInternalName(internalName: String) = servers.firstOrNull { it.internalName == internalName }
	}

	fun sendAsync(jsonObject: JsonObject, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) = SocketUtils.sendAsync(jsonObject, host, socketPort, success, error)
	fun send(jsonObject: JsonObject) = SocketUtils.send(jsonObject, host, socketPort)
}