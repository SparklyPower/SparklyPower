package net.perfectdreams.dreamtorredamorte.listeners

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class PlayerListener(val m: DreamTorreDaMorte) : Listener {
    @EventHandler
    fun onCorreiosItemReceive(event: CorreiosItemReceivingEvent) {
        if (event.player in m.torreDaMorte.players)
            event.result = CorreiosItemReceivingEvent.PlayerInEventResult
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity.location.world.name != "TorreDaMorte")
            return

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
            m.torreDaMorte.removeFromGame(event.entity, skipFinishCheck = false)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (event.player.location.world.name == "TorreDaMorte")
            event.respawnLocation = DreamCore.INSTANCE.spawn!!
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.location.world.name != "TorreDaMorte")
            return

        if (m.torreDaMorte.isStarted) {
            m.torreDaMorte.removeFromQueue(event.player)
            m.torreDaMorte.removeFromGame(event.player, skipFinishCheck = false)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.location.world.name == "TorreDaMorte")
            event.player.teleportToServerSpawnWithEffects()
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!m.torreDaMorte.isStarted)
            return

        if (!event.displaced)
            return

        if (m.torreDaMorte.isStarted) {
            if (event.player.location.world.name != "TorreDaMorte") {
                if (event.player in m.torreDaMorte.players) {
                    // Player está na lista de membros da torre, mas não está na torre da morte!
                    m.torreDaMorte.removeFromGame(event.player, skipFinishCheck = false)
                }
                return
            }

            if (100 >= event.player.location.y)
                m.torreDaMorte.removeFromGame(event.player, skipFinishCheck = false)
        }
    }

    @EventHandler
    fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == "TorreDaMorte")
            e.isCancelled = true
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.player in m.torreDaMorte.players && e.message.split(" ").first().removePrefix("/").lowercase() !in DreamCore.dreamConfig.allowedCommandsDuringEvents)
            e.isCancelled = true
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (e.player.world.name != "TorreDaMorte")
            return

        if (!(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
            return

        // Cancel teleport if it is chorus fruit or ender pearls
        e.isCancelled = true
    }
}