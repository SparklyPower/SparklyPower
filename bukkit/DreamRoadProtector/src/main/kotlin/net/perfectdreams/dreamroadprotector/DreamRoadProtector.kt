package net.perfectdreams.dreamroadprotector

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcustomitems.utils.CustomBlocks
import org.bukkit.*
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class DreamRoadProtector : KotlinPlugin(), Listener {
	private val lastLocations = WeakHashMap<Player, Location>()
	private val walkingOnRoadWithSpeed = SparklyNamespacedBooleanKey("is_on_road_with_speed")
	private val migratedToTheNewRoad = SparklyNamespacedBooleanKey("migrated_to_the_new_road")
	private val migratedToTheNewNewRoad = SparklyNamespacedBooleanKey("migrated_to_the_new_new_road")
	private val worldMigrations = setOf(
		"world",
		"Survival2"
	)

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)

		scheduler().schedule(this) {
			while (true) {
				for (player in Bukkit.getOnlinePlayers()) {
					val lastLocation = lastLocations[player]

					if (lastLocation != null) {
						// Vamos ignorar se o player não moveu
						if (lastLocation.x == player.location.x && lastLocation.y == player.location.y && lastLocation.z == player.location.z)
							continue
					}

					if (player.gameMode != GameMode.SURVIVAL)
						continue

					lastLocations[player] = player.location

					val hasRoadNearby = hasRoadNearby(player.location)

					// We don't use potion effects because it gets VERY annoying on higher speeds
					if (hasRoadNearby) {
						val alreadyHasSpeedApplied = player.persistentDataContainer.get(walkingOnRoadWithSpeed)
						player.persistentDataContainer.set(walkingOnRoadWithSpeed, true)
						player.foodLevel = 20
						if (!alreadyHasSpeedApplied) {
							player.walkSpeed = 0.4f
						}
					} else if (player.persistentDataContainer.get(walkingOnRoadWithSpeed)) {
						player.persistentDataContainer.remove(walkingOnRoadWithSpeed)
						player.walkSpeed = 0.2f
					}
				}

				waitFor(80)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onChunkLoad(e: ChunkLoadEvent) {
		if (e.world.name in worldMigrations) {
			if (!e.chunk.persistentDataContainer.get(migratedToTheNewNewRoad)) {
				val hasMigratedToBookshelfs = e.chunk.persistentDataContainer.get(migratedToTheNewRoad)

				for (x in 0 until 16) {
					for (z in 0 until 16) {
						for (y in e.world.minHeight until e.world.maxHeight) {
							val block = e.chunk.getBlock(x, y, z)
							if (hasMigratedToBookshelfs) {
								if (block.type == Material.CHISELED_BOOKSHELF) {
									val state = block.state as ChiseledBookshelf
									if (state.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY) == CustomBlocks.ASPHALT_SERVER.id)
										block.type = Material.SPARKLYPOWER_ASPHALT_SERVER
								}
							} else {
								if (block.type == Material.BLACK_CONCRETE) {
									block.type = Material.SPARKLYPOWER_ASPHALT_SERVER
								}
							}
						}
					}
				}
				logger.info("Migrated black concrete blocks in chunk ${e.chunk.x} ${e.chunk.z} in world ${e.world.name} (NEW VERSION)")
				// set both
				e.chunk.persistentDataContainer.set(migratedToTheNewRoad, true)
				e.chunk.persistentDataContainer.set(migratedToTheNewNewRoad, true)
			}
		}
	}

	@EventHandler
	fun onTeleport(e: PlayerTeleportEvent) {
		val player = e.player

		// Automatically reset the speed if they teleport
		if (player.persistentDataContainer.get(walkingOnRoadWithSpeed)) {
			player.persistentDataContainer.remove(walkingOnRoadWithSpeed)
			player.isSprinting = false
			player.walkSpeed = 0.2f
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		lastLocations[e.player] = null
	}

	@EventHandler
	fun onBreak(e: BlockBreakEvent) {
		if (e.player.hasPermission("dreamroadprotector.bypass"))
			return

		val hasRoadNearby = hasRoadNearby(e.block.location)

		if (hasRoadNearby) {
			e.isCancelled = true

			e.player.sendMessage("§cVocê não pode construir aqui!")
			e.player.world.spawnParticle(Particle.ANGRY_VILLAGER, e.block.location.add(0.5, 0.0, 0.5), 20, 0.5, 0.5, 0.5)
		}
	}

	@EventHandler
	fun onPlace(e: BlockPlaceEvent) {
		if (e.player.hasPermission("dreamroadprotector.bypass"))
			return

		val hasRoadNearby = hasRoadNearby(e.block.location)

		if (hasRoadNearby) {
			e.isCancelled = true

			e.player.sendMessage("§cVocê não pode construir aqui!")
			e.player.world.spawnParticle(Particle.ANGRY_VILLAGER, e.block.location.add(0.5, 0.0, 0.5), 20, 0.5, 0.5, 0.5)
		}
	}

	@EventHandler
	fun onBlockFromTo(e: BlockFromToEvent) {
		val location = e.toBlock.location

		if (hasRoadNearby(location)) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onBlockForm(e: BlockFormEvent) {
		if (e.newState.block.type == Material.BLACK_CONCRETE_POWDER) {
			e.isCancelled = true
		}
	}

	fun hasRoadNearby(location: Location): Boolean {
		// If it isn't in the survival world, ignore it!
		if (location.world.name != "world" && location.world.name != "Survival2")
			return false

		val x = location.blockX
		val y = location.blockY
		val z = location.blockZ

		for (currentX in x - 3..x + 3) {
			for (currentY in y - 5..y + 5) {
				for (currentZ in z - 3..z + 3) {
					val block = location.world.getBlockAt(currentX, currentY, currentZ)

					val isServerAsphalt = block.type == Material.SPARKLYPOWER_ASPHALT_SERVER || block.type == Material.SPARKLYPOWER_ASPHALT_SERVER_SLAB
					if (isServerAsphalt)
						return true
				}
			}
		}
		return false
	}
}
