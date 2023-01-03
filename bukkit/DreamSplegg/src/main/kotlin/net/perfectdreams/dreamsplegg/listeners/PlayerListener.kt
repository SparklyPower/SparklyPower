package net.perfectdreams.dreamsplegg.listeners

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamsplegg.DreamSplegg
import net.perfectdreams.dreamsplegg.utils.Splegg
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

class PlayerListener(val m: DreamSplegg) : Listener {
    @EventHandler
    fun onCorreiosItemReceive(event: CorreiosItemReceivingEvent) {
        if (event.player in m.splegg.players)
            event.result = CorreiosItemReceivingEvent.PlayerInEventResult
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.location.world.name != Splegg.WORLD_NAME)
            return

        if (!m.splegg.isStarted)
            return

        m.splegg.removeFromQueue(event.player)
        m.splegg.removeFromGame(event.player, skipFinishCheck = false)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.location.world.name == Splegg.WORLD_NAME)
            event.player.teleportToServerSpawnWithEffects()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.location.world.name != Splegg.WORLD_NAME)
            return

        if (!m.splegg.isStarted)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onElytra(e: EntityToggleGlideEvent) {
        if (e.entity.location.world.name != Splegg.WORLD_NAME)
            return

        if (!m.splegg.isStarted)
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!m.splegg.isStarted)
            return

        if (!event.displaced)
            return

        if (m.splegg.isStarted) {
            if (event.player.location.world.name != Splegg.WORLD_NAME) {
                if (event.player in m.splegg.players) {
                    // Player está na lista de membros da torre, mas não está na torre da morte!
                    m.splegg.removeFromGame(event.player, skipFinishCheck = false)
                }
                return
            }

            if (10 >= event.player.location.y)
                m.splegg.removeFromGame(event.player, skipFinishCheck = false)
        }
    }

    @EventHandler
    fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == Splegg.WORLD_NAME)
            e.isCancelled = true
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.player in m.splegg.players)
            e.isCancelled = true
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (e.player.world.name != Splegg.WORLD_NAME)
            return

        if (!(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
            return

        // Cancel teleport if it is chorus fruit or ender pearls
        e.isCancelled = true
    }

    @EventHandler
    fun onTeleport(e: PlayerInteractEvent) {
        if (e.player.world.name != Splegg.WORLD_NAME)
            return

        if (e.item?.type != Material.IRON_SHOVEL)
            return

        if (!m.splegg.canDestroyBlocks)
            return

        // Launch egg
        e.player.launchProjectile(Egg::class.java)
    }

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (e.entity.world.name != Splegg.WORLD_NAME)
            return

        e.isCancelled = true
        val hitBlock = e.hitBlock ?: return

        if (!m.splegg.canDestroyBlocks)
            return

        // I already had times when air blocks were being restored for some reason
        // So let's check if the hitBlock is air before changing it to air
        if (hitBlock.type == Material.AIR)
            return

        m.splegg.blocksToBeRestored[hitBlock.location] = hitBlock.state
        hitBlock.type = Material.AIR

        hitBlock.world.playSound(
            hitBlock.location,
            Sound.ENTITY_CHICKEN_EGG,
            1f,
            DreamUtils.random.nextFloat(0.9f, 1.1f)
        )
    }

    @EventHandler
    fun onEntitySpawn(e: EntitySpawnEvent) {
        if (e.entity.world.name != Splegg.WORLD_NAME)
            return

        if (e.entity.type == EntityType.EGG)
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity.world.name != Splegg.WORLD_NAME)
            return

        e.isCancelled = true
    }
}