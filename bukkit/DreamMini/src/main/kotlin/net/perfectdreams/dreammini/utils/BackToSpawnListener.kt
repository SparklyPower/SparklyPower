package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

/**
 * Makes the player go back to spawn if their current location is unsafe
 */
class BackToSpawnListener(val m: DreamMini) : Listener {
	// Makes the player go back to spawn
	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		// If the player's location is not unsafe, just ignore it!
		if (!e.player.location.isUnsafe)
			return

		// Oh no, it is a unsafe location! Make the player go back to spawn!!
		e.player.teleportToServerSpawnWithEffects()
	}
}