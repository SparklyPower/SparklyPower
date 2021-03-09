package net.perfectdreams.dreamroadprotector

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class DreamRoadProtector : KotlinPlugin(), Listener {
	val lastLocations = WeakHashMap<Player, Location>()

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

					if (hasRoadNearby) {
						player.foodLevel = 20
						player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 5 * 20, 1, true, false))
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
			e.player.world.spawnParticle(Particle.VILLAGER_ANGRY, e.block.location.add(0.5, 0.0, 0.5), 20, 0.5, 0.5, 0.5)
		}
	}

	@EventHandler
	fun onPlace(e: BlockPlaceEvent) {
		if (e.player.hasPermission("dreamroadprotector.bypass"))
			return

		if (e.block.type == Material.BLACK_CONCRETE) {
			e.isCancelled = true
			e.player.sendMessage("§cVocê não pode usar blocos de concreto preto!")
			e.player.world.spawnParticle(Particle.VILLAGER_ANGRY, e.block.location.add(0.5, 0.0, 0.5), 20, 0.5, 0.5, 0.5)
			return
		}

		val hasRoadNearby = hasRoadNearby(e.block.location)

		if (hasRoadNearby) {
			e.isCancelled = true

			e.player.sendMessage("§cVocê não pode construir aqui!")
			e.player.world.spawnParticle(Particle.VILLAGER_ANGRY, e.block.location.add(0.5, 0.0, 0.5), 20, 0.5, 0.5, 0.5)
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
		val x = location.blockX
		val y = location.blockY
		val z = location.blockZ

		for (currentX in x - 3..x + 3) {
			for (currentY in y - 5..y + 5) {
				for (currentZ in z - 3..z + 3) {
					val block = location.world.getBlockAt(currentX, currentY, currentZ)

					val isConcrete = block?.type == Material.BLACK_CONCRETE

					if (isConcrete)
						return true
				}
			}
		}
		return false
	}
}
