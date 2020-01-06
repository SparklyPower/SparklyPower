package net.perfectdreams.dreamlobbyfun.utils

import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.schedule
import net.citizensnpcs.api.CitizensAPI
import net.perfectdreams.dreamcore.utils.ArmorStandHologram
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Bukkit

class ServerCitizen(val citizenId: Int, val serverName: String, val fancyServerName: String) {
	@Transient
	var playerCountHologram: ArmorStandHologram? = null
	@Transient
	var serverNameHologram: ArmorStandHologram? = null
	@Transient
	var clickHereHologram: ArmorStandHologram? = null

	var easeTask: CoroutineTask? = null
	var currentEase = 0.0
	var positive = true

	fun update() {
		val citizen = CitizensAPI.getNPCRegistry().getById(citizenId) ?: run {
			println("Citizen ${citizenId} não existe!")
			return
		}

		if (citizen.entity == null) { // Se é null, quer dizer que o NPC ainda não nasceu
			println("Citizen ${citizenId} ainda não nasceu!")
			return
		}

		val holoLocation = citizen.entity.location.clone().add(0.0, 2.1, 0.0)

		if (playerCountHologram == null) {
			val playerCountHologram = ArmorStandHologram(
					holoLocation.clone().add(0.0, -0.285, 0.0),
					"§7??? players online"
			)

			playerCountHologram.spawn()
			this.playerCountHologram = playerCountHologram
		}

		if (serverNameHologram == null) {
			val serverNameHologram = ArmorStandHologram(
					holoLocation,
					"§a§l$fancyServerName".translateColorCodes()
			)
			serverNameHologram.spawn()
			this.serverNameHologram = serverNameHologram
		}

		if (clickHereHologram == null) {
			val clickHereHologram = ArmorStandHologram(
					holoLocation.clone().add(0.0, 0.5, 0.0),
					"§6§l» §a§lCLIQUE AQUI §6§l«"
			)

			clickHereHologram.spawn()
			this.clickHereHologram = clickHereHologram
		}

		val playerCountHologram = playerCountHologram!!
		val serverNameHologram = serverNameHologram!!
		val clickHereHologram = clickHereHologram!!

		val middle = holoLocation.clone().add(0.0, 0.5, 0.0)

		if (serverNameHologram.location != holoLocation) {
			easeTask?.cancel()
			easeTask = null
			playerCountHologram.teleport(holoLocation.clone().add(0.0, -0.285, 0.0))
			serverNameHologram.teleport(holoLocation)
			clickHereHologram.teleport(middle)
		}

		if (easeTask == null) {
			easeTask = scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamLobbyFun")) {
				while (true) {
					val newLocation = middle.clone()
					clickHereHologram.teleport(newLocation.add(0.0, (ease(currentEase) / 4) - 0.125, 0.0))
					if (positive)
						currentEase += 0.1
					else
						currentEase -= 0.1
					if (currentEase == 1.0)
						positive = false
					if (currentEase == 0.0)
						positive = true
					waitFor(2)
				}
			}
		}

		val playerCount = DreamLobbyFun.SERVER_ONLINE_COUNT[serverName]

		if (playerCount == null) {
			playerCountHologram.setLine("§7??? players online")
		} else {
			val singular = playerCount == 1

			if (singular) {
				playerCountHologram.setLine("§7$playerCount player online")
			} else {
				playerCountHologram.setLine("§7$playerCount players online")
			}
		}
	}

	fun ease(x: Double): Double {
		return (Math.cos(Math.PI * x) + 1) / 2
	}
}