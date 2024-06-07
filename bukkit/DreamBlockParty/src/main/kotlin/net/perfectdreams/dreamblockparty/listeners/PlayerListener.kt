package net.perfectdreams.dreamblockparty.listeners

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamblockparty.DreamBlockParty
import net.perfectdreams.dreamblockparty.utils.BlockParty
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.Egg
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.*

class PlayerListener(val m: DreamBlockParty) : Listener {
    /* @EventHandler
    fun onCorreiosItemReceive(event: CorreiosItemReceivingEvent) {
        if (event.player in m.blockParty.players)
            event.result = CorreiosItemReceivingEvent.PlayerInEventResult
    } */

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.location.world.name != BlockParty.WORLD_NAME)
            return

        if (!m.blockParty.isStarted)
            return

        m.blockParty.removeFromQueue(event.player)
        m.blockParty.removeFromGame(event.player, skipFinishCheck = false)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.location.world.name == BlockParty.WORLD_NAME)
            event.player.teleportToServerSpawnWithEffects()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.location.world.name != BlockParty.WORLD_NAME)
            return

        if (!m.blockParty.isStarted)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onElytra(e: EntityToggleGlideEvent) {
        if (e.entity.location.world.name != BlockParty.WORLD_NAME)
            return

        if (!m.blockParty.isStarted)
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!m.blockParty.isStarted)
            return

        if (!event.displaced)
            return

        if (event.player.location.world.name == BlockParty.WORLD_NAME) {

            if (70 >= event.player.location.y) {
                m.blockParty.removeFromGame(event.player, skipFinishCheck = false)
            } else {
                if (event.player in m.blockParty.playersThatProbablyAlreadyLost && event.player.location.block.getRelative(BlockFace.DOWN).isSolid) {
                    for (failSound in m.blockParty.funnyFailSounds) {
                        event.player.stopSound(failSound)
                    }
                    event.player.playSound(event.player, m.blockParty.funnyEpicRecoverySounds.random(), 10f, 1f)
                    m.blockParty.playersThatProbablyAlreadyLost.remove(event.player)
                }
            }
        } else {
            if (event.player in m.blockParty.players) {
                // Player está na lista de membros da torre, mas não está na torre da morte!
                m.blockParty.removeFromGame(event.player, skipFinishCheck = false)
            }
            return
        }
    }

    @EventHandler
    fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == BlockParty.WORLD_NAME)
            e.isCancelled = true
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.player in m.blockParty.players)
            e.isCancelled = true
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (e.player.world.name != BlockParty.WORLD_NAME)
            return

        if (!(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
            return

        // Cancel teleport if it is chorus fruit or ender pearls
        e.isCancelled = true
    }

    @EventHandler
    fun onEntitySpawn(e: EntitySpawnEvent) {
        if (e.entity.world.name != BlockParty.WORLD_NAME)
            return

        if (e.entity.type == EntityType.TEXT_DISPLAY || e.entity.type == EntityType.FIREWORK_ROCKET)
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity.world.name != BlockParty.WORLD_NAME)
            return

        e.isCancelled = true
    }


    @EventHandler
    fun onDamage(e: PlayerDropItemEvent) {
        if (e.player.world.name != BlockParty.WORLD_NAME)
            return

        e.isCancelled = true
    }
}