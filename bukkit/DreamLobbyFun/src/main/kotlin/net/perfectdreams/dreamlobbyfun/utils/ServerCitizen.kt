package net.perfectdreams.dreamlobbyfun.utils

import kotlinx.coroutines.Job
import me.filoghost.holographicdisplays.api.hologram.Hologram
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine
import net.citizensnpcs.api.CitizensAPI
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Bukkit
import kotlin.math.cos

class ServerCitizen(
	val data: ServerCitizenData,
	val m: DreamLobbyFun
) {
	var playerCountHologram: Hologram? = null
	var serverNameHologram: Hologram? = null
	var clickHereHologram: Hologram? = null

	var easeTask: Job? = null
	var animationTicks = 0

	fun update() {
		val citizen = CitizensAPI.getNPCRegistry().getById(data.citizenId) ?: run {
			m.logger.warning { "Citizen ${data.citizenId} não existe!" }
			return
		}

		if (citizen.entity == null) { // Se é null, quer dizer que o NPC ainda não nasceu
			m.logger.warning { "Citizen ${data.citizenId} ainda não nasceu!" }
			return
		}

		val holoLocation = citizen.entity.location.clone().add(0.0, 3.1, 0.0)

		if (playerCountHologram == null) {
			playerCountHologram = m.holographicDisplaysAPI.createHologram(holoLocation.clone().add(0.0, -0.285, 0.0))
				.apply {
					this.lines.appendText("§7??? players online")
				}
		}

		if (serverNameHologram == null) {
			serverNameHologram = m.holographicDisplaysAPI.createHologram(holoLocation)
				.apply {
					this.lines.appendText("§a§l${data.fancyServerName}".translateColorCodes())
				}
		}

		if (clickHereHologram == null) {
			clickHereHologram = m.holographicDisplaysAPI.createHologram(holoLocation.clone().add(0.0, 0.5, 0.0))
				.apply {
					this.lines.appendText("§6§l» §a§lCLIQUE AQUI §6§l«")
				}
		}

		val playerCountHologram = playerCountHologram!!
		val serverNameHologram = serverNameHologram!!
		val clickHereHologram = clickHereHologram!!

		val middle = holoLocation.clone().add(0.0, 0.5, 0.0)

		if (easeTask == null) {
			easeTask = m.launchMainThread {
				while (true) {
					val newLocation = middle.clone()
					val mod = animationTicks % 32

					clickHereHologram.setPosition(
						newLocation.add(
							0.0,
							if (mod > 16) {
								-0.285 + easeInOutSine((mod - 16) / 16.0) * 0.285
							} else {
								easeInOutSine(mod / 16.0) * -0.285
							},
							0.0
						)
					)

					animationTicks++
					delayTicks(2)
				}
			}
		}

		val playerCount = DreamLobbyFun.SERVER_ONLINE_COUNT[data.serverName]

		if (playerCount == null) {
			(playerCountHologram.lines.get(0) as TextHologramLine)
				.text = "§7??? players online"
		} else {
			val singular = playerCount == 1

			if (singular) {
				(playerCountHologram.lines.get(0) as TextHologramLine)
					.text = "§7$playerCount player online"
			} else {
				(playerCountHologram.lines.get(0) as TextHologramLine)
					.text = "§7$playerCount players online"
			}
		}
	}

	fun easeInOutSine(x: Double): Double {
		return -(cos(Math.PI * x) - 1) / 2
	}
}