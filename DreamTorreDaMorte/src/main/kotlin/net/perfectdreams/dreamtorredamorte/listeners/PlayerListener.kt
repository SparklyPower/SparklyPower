package net.perfectdreams.dreamtorredamorte.listeners

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
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
    fun onRespawn(event: PlayerRespawnEvent) {
        if (event.player.location.world.name == "TorreDaMorte")
            event.respawnLocation = DreamCore.dreamConfig.getSpawn()
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
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.location.world.name == "TorreDaMorte")
            event.player.teleportToServerSpawn()
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
                    m.torreDaMorte.removeFromGame(event.player)
                }
                return
            }

            if (20 >= event.player.location.y)
                m.torreDaMorte.removeFromGame(event.player)
        }
    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        if (!m.torreDaMorte.isStarted)
            return

        // Se o player teletransportar para fora da torre da morte, vamos restaurar as coisas dela
        // Mas como é para "fora", vamos apenas ignorar qualquer teleport para o spawn e coisas assim
        if (event.from.world.name == "TorreDaMorte" && event.to.world.name != "TorreDaMorte") {
            if (event.player in m.torreDaMorte.players) {
                // Então ele foi teletransportado para fora, mas está na lista de players!
                // Então vamos fazer que ela tenha "saido" do evento, mas não iremos teletransportá-lo
                m.torreDaMorte.removeFromGame(event.player, false)
            }
        }
    }

    @EventHandler
    fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == "TorreDaMorte")
            e.isCancelled = true
    }
}