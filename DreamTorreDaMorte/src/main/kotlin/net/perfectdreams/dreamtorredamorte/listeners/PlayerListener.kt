package net.perfectdreams.dreamtorredamorte.listeners

import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerListener(val m: DreamTorreDaMorte) : Listener {
    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager

        if (entity is Player && damager is Player) {
            m.torreDaMorte.lastHits[entity] = damager
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity.location.world.name != "TorreDaMorte")
            return

        if (m.torreDaMorte.isStarted) {
            if (!m.torreDaMorte.canAttack) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onDie(event: PlayerDeathEvent) {
        if (event.entity.location.world.name != "TorreDaMorte")
            return

        if (m.torreDaMorte.isStarted) {
            m.torreDaMorte.removeFromQueue(event.entity)
            m.torreDaMorte.removeFromGame(event.entity)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.location.world.name != "TorreDaMorte")
            return

        if (m.torreDaMorte.isStarted) {
            m.torreDaMorte.removeFromQueue(event.player)
            m.torreDaMorte.removeFromGame(event.player)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.player.location.world.name != "TorreDaMorte")
            return

        if (!event.displaced)
            return

        if (m.torreDaMorte.isStarted) {
            if (0 >= event.player.location.y) {
                event.player.damage(1000000.0)
            }
        }
    }
}