package net.perfectdreams.dreamrestarter

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamrestarter.commands.RestartCommand
import java.io.File
import java.util.*

class DreamRestarter : KotlinPlugin() {
	val storedPlayerRestart = File(dataFolder, "players_before_restart")

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		registerCommand(RestartCommand(this))

		scheduler().schedule(this) {
			waitFor(20L) // wait one second just to avoid other plugins still "setting up" after server load

			DreamNetwork.PERFECTDREAMS_LOBBY.sendAsync(
				jsonObject(
					"type" to "serverUp",
					"serverName" to DreamCore.dreamConfig.bungeeName
				)
			)

			// Enviar para o BungeeCord que já pode transferir todos os players de volta para o servidor :3
			if (storedPlayerRestart.exists()) {
				val uniqueIds = storedPlayerRestart.readLines().map { UUID.fromString(it) }

				storedPlayerRestart.delete()

				for (uniqueId in uniqueIds) {
					DreamNetwork.PERFECTDREAMS_BUNGEE.sendAsync(
						jsonObject(
							"type" to "transferPlayersByUUID",
							"bungeeServer" to DreamCore.dreamConfig.bungeeName,
							"players" to listOf(uniqueId.toString()).toJsonArray()
						)
					)
				}

				waitFor(10L)
			}
		}
	}
}