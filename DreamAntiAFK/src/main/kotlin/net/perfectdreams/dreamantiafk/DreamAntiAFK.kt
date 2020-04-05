package net.perfectdreams.dreamantiafk

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.util.*

class DreamAntiAFK : KotlinPlugin(), Listener {
	val players = WeakHashMap<Player, PlayerAFKInfo>()
	var blockedWorlds = setOf<String>()

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()
		val configFile = File(dataFolder, "blockedworlds.json")

		if (configFile.exists()) {
			blockedWorlds = DreamUtils.gson.fromJson(configFile.readText())
		}

		registerEvents(this)
		registerCommand(object: AbstractCommand("dreamantiafk", permission = "dreamantiafk.setup") {
			@Subcommand
			fun onCommand(p0: Player) {
				blockedWorlds = DreamUtils.gson.fromJson(configFile.readText())

				p0.sendMessage("§aRecarregado com sucesso!")
			}
		})

		scheduler().schedule(this) {
			while (true) {
				waitFor(60 * 20)

				// okay, AntiAFK:tm:
				for (player in Bukkit.getOnlinePlayers().filter { !blockedWorlds.contains(it.world.name) && !it.hasPermission("perfectdreams.bypassantiafk") }) {
					try {
						val pair = players.getOrDefault(player, null)

						if (pair != null) {
							val location = player.location
							val lastLocation = pair.location

							val distance = location.distance(lastLocation)
							pair.location = location

							var value = 5
							val isLiquid = player.location.block.isLiquid
							val isInsideVehicle = player.isInsideVehicle

							if (isLiquid || isInsideVehicle) {
								value = 100
							}

							if (value >= distance) {
								if (pair.score >= 10) {
									// omg
									player.kickPlayer(
											"""§4Desconectado§c automaticamente pelo Servidor!
											|
											|Motivo:
											|§aFicar parado por muito tempo!§f
											|
											|§6(§eObs:§6 Não precisa ficar preocupado!
											|§6Você ainda pode entrar no PerfectDreams! Nada irá acontecer!)""".trimMargin()
									)

									for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission(isStaffPermission) }) {
										staff.sendMessage("§b${player.name} §3foi kickado por ficar parado por mais de 10 minutos!")
										if (isLiquid) {
											staff.sendMessage("§bUso de AntiAFK? §3Provavelmente! §7(Água @ §b${player.location.blockX}§3, §b${player.location.blockY}§3, §b${player.location.blockZ}§7)")
											staff.sendMessage("§7(Obs: Não é necessário punir o usuário!)")
										} else if (isInsideVehicle) {
											staff.sendMessage("§bUso de AntiAFK? §3Provavelmente! §7(Dentro de um veículo @ §b${player.location.blockX}§3, §b${player.location.blockY}§3, §b${player.location.blockZ}§7)")
											staff.sendMessage("§7(Obs: Não é necessário punir o usuário!)")
										} else {
											staff.sendMessage("§bUso de AntiAFK? §3Não!")
										}
									}
								}
								pair.score += 1
							} else {
								pair.score = Math.max(pair.score - 1, 0)
							}
						} else {
							players.put(player, PlayerAFKInfo(player.location, 0))
						}
					} catch (e: IllegalArgumentException) {} // Mundos diferentes
				}
			}
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		players.remove(e.player)
	}

	@EventHandler
	fun onChat(e: AsyncPlayerChatEvent) {
		if (players.containsKey(e.player)) {
			val info = players[e.player]!!
			info.score = Math.max(info.score - 1, 0)
		}
	}

	@EventHandler
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (players.containsKey(e.player)) {
			val info = players[e.player]!!
			info.score = Math.max(info.score - 1, 0)
		}
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEvent) {
		if (players.containsKey(e.player)) {
			val info = players[e.player]!!
			info.score = Math.max(info.score - 1, 0)
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	class PlayerAFKInfo(var location: Location, var score: Int)
}