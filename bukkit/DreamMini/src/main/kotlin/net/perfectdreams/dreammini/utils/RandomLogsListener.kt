package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.logging.Level

class RandomLogsListener(val m: DreamMini) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onTeleport(e: PlayerTeleportEvent) {
        try {
            error("Throwing when teleporting just to catch it later")
        } catch (exception: Exception) {
            m.logger.log(Level.INFO, exception) { "Player ${e.player.name} (${e.player.uniqueId}) teleported due to ${e.cause} from ${e.from.world.name} (${e.from.x}, ${e.from.y}, ${e.from.z}) to ${e.to.world.name} (${e.to.x}, ${e.to.y}, ${e.to.z}) (cancelled? ${e.isCancelled})" }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onDeath(e: PlayerDeathEvent) {
        m.logger.info("Player ${e.player.name} (${e.player.uniqueId}) died at ${e.player.world.name} (${e.player.x}, ${e.player.y}, ${e.player.z}) (cancelled? ${e.isCancelled})")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onQuit(e: PlayerQuitEvent) {
        m.logger.info("Player ${e.player.name} (${e.player.uniqueId}) disconnected at ${e.player.world.name} (${e.player.x}, ${e.player.y}, ${e.player.z})")
    }
}