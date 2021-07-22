package net.perfectdreams.dreamantiafk

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.schedule
import io.papermc.paper.event.player.AsyncChatEvent
import net.perfectdreams.dreamantiafk.listeners.FishListener
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.leftClick
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.*
import java.io.File
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class DreamAntiAFK : KotlinPlugin(), Listener {
	val players = WeakHashMap<Player, PlayerAFKInfo>()
	val logoutTime = Caffeine.newBuilder()
		.expireAfterWrite(1, TimeUnit.MINUTES)
		.build<UUID, Long>()
		.asMap()

	var blockedWorlds = setOf<String>()

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()
		val configFile = File(dataFolder, "blockedworlds.json")

		if (configFile.exists()) {
			blockedWorlds = DreamUtils.gson.fromJson(configFile.readText())
		}

		val fishListener = FishListener()
		registerEvents(this)
		registerEvents(fishListener)

		registerCommand(command("DreamAntiAFKCommand", listOf("dreamantiafk")) {
			permission = "dreamantiafk.see"

			executes {
				sender.sendMessage("§6Stats de Players AFK:")
				players.entries.sortedBy { it.value.score }.forEach { (player, info) ->
					sender.sendMessage("§b${player.name}§e: ${info.score}/100 pontos")
				}
			}
		})

		registerCommand(command("DreamAntiAFKReloadCommand", listOf("dreamantiafk reload")) {
			permission = "dreamantiafk.setup"

			executes {
				blockedWorlds = DreamUtils.gson.fromJson(configFile.readText())

				sender.sendMessage("§aRecarregado com sucesso!")
			}
		})

		// TODO: Move to a different plugin
		registerCommand(command("DreamAntiAFKFishCommand", listOf("dreamantiafk fish")) {
			permission = "dreamantiafk.see"

			executes {
				val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
					.withZone(ZoneId.systemDefault())
				val playerName = args.getOrNull(0) ?: throw CommandException("Coloque o nome do player!")

				val player = Bukkit.getPlayerUniqueId(playerName)
				val playerFishEvents = fishListener.fishEvents[player] ?: throw CommandException("Não tem nenhum evento de peixe para o player específicado!")

				sender.sendMessage("§6Eventos de Peixe do $playerName:")

				for ((index, event) in playerFishEvents.withIndex()) {
					val previousEvent = playerFishEvents.getOrNull(index - 1)

					if (previousEvent != null) {
						val diff = event.time - previousEvent.time
						val diffInSeconds = diff / 1000.0
						sender.sendMessage("§7+${diffInSeconds}s")
					}

					Instant.ofEpochMilli(event.time)
					sender.sendMessage("§7[${DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(event.time).atZone(ZoneId.of("America/Sao_Paulo")))}] §eEvento: ${event.state}")
				}
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

							if (location.world != lastLocation.world) {
								// If the worlds are different, we are going to reset the AFK score and update the location
								pair.score = 0
								pair.location = location
								continue
							}

							if (location.isWithinRegion("spawn")) // Do not kick users in the spawn region, allows them to write stuff happily
								continue

							val distance = location.distance(lastLocation)
							val distanceSameYLevel = location.clone().apply { this.y = lastLocation.y }
								.distance(lastLocation)

							pair.location = location

							var value = 8 // Only increase score if the user is around 8 blocks
							val isLiquid = player.location.block.isLiquid
							val isInsideVehicle = player.isInsideVehicle

							if (isLiquid || isInsideVehicle) {
								value = 100 // But if they are inside a vehicle or in a liquid, let's increase it to 100!
							}

							when {
								distance == 0.0 -> {
									logger.info("Player ${player.name} haven't moved in a long time, adding 20 to the player's score... Distance: $distance")
									pair.score += 10 // Haven't moved in a long time, so let's increase +20 to the player's score!
								}

								value >= distance -> {
									val scoreToBeAdded = (((value - distance) / value) * 10).toInt()
										.coerceAtMost(10)
									logger.info("Player ${player.name} has moved but just a little bit, adding $scoreToBeAdded to the player's score... Distance: $distance")

									// Changes depending on the difference between the distance, if the user haven't moved a lot, it grows more
									pair.score += scoreToBeAdded
								}

								value >= distanceSameYLevel -> {
									val scoreToBeAdded = (((value - distanceSameYLevel) / value) * 10).toInt()
										.coerceAtMost(10)
									logger.info("Player ${player.name} has moved but we are only matching the distance at the same Y level, adding $scoreToBeAdded to the player's score... Distance at Y level: $distanceSameYLevel")

									// Same thing as above, but because only the X/Z axis are similar, we add less score
									pair.score += scoreToBeAdded
								}

								else -> {
									// Now, we are going to decrease the score if nothing matches
									// However we are going to decrease depending on the distance travelled!
									val scoreToBeRemoved = ((distance / value) * 10)
										.toInt()
										.coerceAtMost(10) // Max 10 score per Anti AFK check

									logger.info("Player ${player.name} moved, so we are going to decrease $scoreToBeRemoved from the player's score... Distance: $distance")

									pair.score = (pair.score - scoreToBeRemoved)
										.coerceAtLeast(0)
								}
							}

							if (pair.score >= 100) {
								// omg
								player.kickPlayer(
									"""§4Desconectado§c automaticamente pelo Servidor!
											|
											|Motivo:
											|§aFicar parado por muito tempo!§f
											|
											|§6(§eObs:§6 Não precisa ficar preocupado!
											|§6Você ainda pode entrar no SparklyPower! Nada irá acontecer!)""".trimMargin()
								)
								logoutTime[player.uniqueId] = System.currentTimeMillis()

								for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission(isStaffPermission) }) {
									staff.sendMessage("§b${player.name} §3foi kickado por ficar parado por muito tempo!")
									when {
										isLiquid -> {
											staff.sendMessage("§bUso de AntiAFK? §3Provavelmente! §7(Água @ §b${player.location.blockX}§3, §b${player.location.blockY}§3, §b${player.location.blockZ}§7)")
											staff.sendMessage("§7(Obs: Não é necessário punir o usuário!)")
										}
										isInsideVehicle -> {
											staff.sendMessage("§bUso de AntiAFK? §3Provavelmente! §7(Dentro de um veículo @ §b${player.location.blockX}§3, §b${player.location.blockY}§3, §b${player.location.blockZ}§7)")
											staff.sendMessage("§7(Obs: Não é necessário punir o usuário!)")
										}
										else -> {
											staff.sendMessage("§bUso de AntiAFK? §3Não!")
										}
									}
								}
							}
						} else {
							players[player] = PlayerAFKInfo(player.location, 0)
						}
					} catch (e: IllegalArgumentException) {} // Mundos diferentes
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onJoin(e: PlayerJoinEvent) {
		val whenLoggedOutDueToAfkKick = logoutTime[e.player.uniqueId]

		if (whenLoggedOutDueToAfkKick != null) {
			for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission(isStaffPermission) }) {
				staff.sendMessage("§b${e.player.name} §3foi kickado por AFK, mas ele voltou em menos de um minuto! Talvez ele esteja tentando burlar o sistema de AFK!!")
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onQuit(e: PlayerQuitEvent) {
		players.remove(e.player)
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onChat(e: AsyncChatEvent) {
		if (players.containsKey(e.player)) {
			val info = players[e.player]!!
			// Decrease the score a bit if they are talking in chat
			// If they send 2 messages per minute, it is already enough
			info.score = Math.max(info.score - 5, 0)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (players.containsKey(e.player)) {
			val info = players[e.player]!!
			// Decrease the score a bit if they are talking in chat
			// If they send 4 commands per minute, it is already enough
			info.score = Math.max(info.score - 3, 0)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onBlockPlace(e: BlockPlaceEvent) {
		if (players.containsKey(e.player)) {
			// Decrease it a bit if they are placing blocks
			val info = players[e.player]!!
			info.score = Math.max(info.score - 1, 0)
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	fun onInteract(e: PlayerInteractEvent) {
		if (players.containsKey(e.player) && (e.rightClick || e.leftClick)) {
			// Decrease it a bit if they are placing blocks
			val info = players[e.player]!!
			info.score = Math.max(info.score - 1, 0)
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	class PlayerAFKInfo(var location: Location, var score: Int)
}