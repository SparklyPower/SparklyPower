package net.perfectdreams.dreamlabirinto.listeners

import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamlabirinto.DreamLabirinto
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

class MoveListener(val m: DreamLabirinto) : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (e.player.world != m.event.startLocation?.world)
            return

        if (!e.displaced)
            return

        if (m.event.startCooldown > 0)
            e.isCancelled = true
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.player.world != m.event.startLocation?.world)
            return

        // Block all item interaction in the event
        e.isCancelled = true
    }
}