package net.perfectdreams.dreamrestarter

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.okkero.skedule.schedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.tables.Transactions.time
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamrestarter.commands.RestartCommand
import net.sparklypower.rpc.SparklyBungeeRequest
import org.bukkit.Bukkit
import java.io.File
import java.time.*
import java.util.*
import kotlin.time.Duration.Companion.seconds

class DreamRestarter : KotlinPlugin() {
	companion object {
		val RESTART_DELAY = 2.seconds
		private val SERVER_ZONE_ID = ZoneId.of("America/Sao_Paulo")
	}

	val storedPlayerRestart = File(dataFolder, "players_before_restart")

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		registerCommand(RestartCommand(this))

		scheduler().schedule(this) {
			launchAsyncThread {
				val now = ZonedDateTime.now()
				val today = ZonedDateTime.now(SERVER_ZONE_ID)
				val todayAtTime = ZonedDateTime.of(today.toLocalDate(), LocalTime.of(5, 0), SERVER_ZONE_ID)
				val gonnaBeScheduledAtTime =  if (now > todayAtTime) {
					// If today at time is larger than today, then it means that we need to schedule it for tomorrow
					todayAtTime.plusDays(1)
				} else todayAtTime

				val diff = gonnaBeScheduledAtTime.toInstant().toEpochMilli() - System.currentTimeMillis()

				logger.info("Server will restart in ${diff}ms ($gonnaBeScheduledAtTime)")

				delay(diff - 120_000)

				Bukkit.broadcastMessage("§aServidor irá reiniciar em dois minutos!")

				delay(60_000)

				Bukkit.broadcastMessage("§aServidor irá reiniciar em um minuto!")

				delay(30_000)

				Bukkit.broadcastMessage("§aServidor irá reiniciar em 30s!")

				delay(15_000)

				Bukkit.broadcastMessage("§aServidor irá reiniciar em 15s!")

				delay(10_000)

				Bukkit.broadcastMessage("§aServidor irá reiniciar em 5s!")

				delay(1_000)
				Bukkit.broadcastMessage("§aServidor irá reiniciar em 4s!")

				delay(1_000)
				Bukkit.broadcastMessage("§aServidor irá reiniciar em 3s!")

				delay(1_000)
				Bukkit.broadcastMessage("§aServidor irá reiniciar em 2s!")

				delay(1_000)
				Bukkit.broadcastMessage("§aServidor irá reiniciar em 1s!")

				Bukkit.broadcastMessage("§aServidor está reiniciando... bye bye, te vejo em breve! :3")

				storeCurrentPlayersAndSendServerDownNotification()

				// Wait before *really* shutting down
				delay(RESTART_DELAY)

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

				launchAsyncThread {
					DreamCore.INSTANCE.rpc.bungeeCord.send(
						SparklyBungeeRequest.TransferPlayersRequest(
							// Transfer the players to us!
							uniqueIds.map { it.toString() },
							SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerNameTarget(
								DreamCore.dreamConfig.bungeeName
							)
						)
					)
				}
			}
		}
	}

	fun storeCurrentPlayersAndSendServerDownNotification() {
		val onlinePlayersUniqueIds = Bukkit.getOnlinePlayers()
			.map { it.uniqueId }

		storedPlayerRestart
			.writeText(
				onlinePlayersUniqueIds.joinToString("\n") { it.toString() }
			)

		DreamNetwork.PERFECTDREAMS_LOBBY.send(
			jsonObject(
				"type" to "serverDown",
				"serverName" to DreamCore.dreamConfig.bungeeName
			)
		)

		launchAsyncThread {
			DreamCore.INSTANCE.rpc.bungeeCord.send(
				SparklyBungeeRequest.TransferPlayersRequest(
					// Transfer the players to the lobby!
					onlinePlayersUniqueIds.map { it.toString() },
					SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerNameTarget(
						DreamCore.dreamConfig.servers.lobby.bungeeName
					)
				)
			)
		}
	}
}