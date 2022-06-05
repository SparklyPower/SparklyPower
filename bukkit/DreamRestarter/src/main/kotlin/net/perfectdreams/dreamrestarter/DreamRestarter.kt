package net.perfectdreams.dreamrestarter

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.okkero.skedule.schedule
import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.tables.Transactions.time
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamrestarter.commands.RestartCommand
import org.bukkit.Bukkit
import java.io.File
import java.time.*
import java.util.*

class DreamRestarter : KotlinPlugin() {
	val storedPlayerRestart = File(dataFolder, "players_before_restart")

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		registerCommand(RestartCommand(this))

		scheduler().schedule(this) {
			launchAsyncThread {
				val now = Instant.now()
				val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
				val todayAtTime = LocalDateTime.of(today, LocalTime.of(5, 0))
				val gonnaBeScheduledAtTime =  if (now > todayAtTime.toInstant(ZoneOffset.UTC)) {
					// If today at time is larger than today, then it means that we need to schedule it for tomorrow
					todayAtTime.plusDays(1)
				} else todayAtTime

				val diff = gonnaBeScheduledAtTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

				delay(diff)

				logger.info("Server will restart in ${diff}ms")

				storeCurrentPlayersAndSendServerDownNotification()

				// Wait 2.5s before *really* shutting down
				delay(2_500)

				Bukkit.shutdown()
			}
		}

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

	fun storeCurrentPlayersAndSendServerDownNotification() {
		storedPlayerRestart
			.writeText(
				Bukkit.getOnlinePlayers().joinToString("\n") { it.uniqueId.toString() }
			)

		DreamNetwork.PERFECTDREAMS_LOBBY.send(
			jsonObject(
				"type" to "serverDown",
				"serverName" to DreamCore.dreamConfig.bungeeName
			)
		)
	}
}