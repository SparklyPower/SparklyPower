package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Makes the player go back to spawn if their current location is unsafe
 */
class BackToSpawnListener(val m: DreamMini) : Listener {
	// Makes the player go back to spawn
	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		if (e.player.location.isUnsafe) {
			// Oh no, it is an unsafe location! Make the player go back to spawn!!
			m.logger.info("Moved player ${e.player.name} (${e.player.uniqueId}) back to the spawn world because their location was unsafe! (previous location: ${e.player.location.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z})")
			e.player.teleportToServerSpawnWithEffects()
			return
		}

		// If the player's location is in an unsafe world, make it go back to the spawn too
		if (e.player.location.world.name in m.config.getStringList("unsafeWorlds")) {
			// Oh no, it is an unsafe location! Make the player go back to spawn!!
			m.logger.info("Moved player ${e.player.name} (${e.player.uniqueId}) back to the spawn world because their world was unsafe! (previous location: ${e.player.location.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z})")
			e.player.teleportToServerSpawnWithEffects()
			return
		}

		// If the player's location is not unsafe, just ignore it!
	}
}