package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

class NetherTeleportListener(val m: DreamMini) : Listener {
	@EventHandler
	fun onClick(e: PlayerPortalEvent) {
		if (e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			e.isCancelled = true
			e.player.performCommand("warp nether")
		}
	}
}