package net.perfectdreams.dreamtntrun.listeners

import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import net.perfectdreams.dreamtntrun.DreamTNTRun
import net.perfectdreams.dreamtntrun.utils.TNTRun
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*

class PlayerListener(val m: DreamTNTRun) : Listener {
    @EventHandler
    fun onCorreiosItemReceive(event: CorreiosItemReceivingEvent) {
        if (event.player in m.TNTRun.players)
            event.result = CorreiosItemReceivingEvent.PlayerInEventResult
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.location.world.name != TNTRun.WORLD_NAME)
            return

        if (!m.TNTRun.isStarted)
            return

            m.TNTRun.removeFromQueue(event.player)
            m.TNTRun.removeFromGame(event.player, skipFinishCheck = false)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.location.world.name == TNTRun.WORLD_NAME)
            event.player.teleportToServerSpawn()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.location.world.name != TNTRun.WORLD_NAME)
            return

        if (!m.TNTRun.isStarted)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onElytra(e: EntityToggleGlideEvent) {
        if (event.player.location.world.name != TNTRun.WORLD_NAME)
            return

        if (!m.TNTRun.isStarted)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!m.TNTRun.isStarted)
            return

        if (!event.displaced)
            return

        if (m.TNTRun.isStarted) {
            if (event.player.location.world.name != TNTRun.WORLD_NAME) {
                if (event.player in m.TNTRun.players) {
                    // Player está na lista de membros da torre, mas não está na torre da morte!
                    m.TNTRun.removeFromGame(event.player, skipFinishCheck = false)
                }
                return
            }

            if (event.player.location.world.name == TNTRun.WORLD_NAME) {
                val blockBelowThem = event.player.location.block.getRelative(BlockFace.DOWN)
                val blockBelowBelowThem = blockBelowThem.getRelative(BlockFace.DOWN)

                if (blockBelowThem.type != Material.SAND) {
                    if (blockBelowThem.type == Material.DIAMOND_BLOCK)
                        m.TNTRun.removeFromGame(event.player, skipFinishCheck = false)

                    return
                }

                if (m.TNTRun.canDestroyBlocks && !m.TNTRun.blocksToBeRestored.containsKey(blockBelowThem.location)) {
                    m.TNTRun.blocksToBeRestored[blockBelowThem.location] = blockBelowThem.state
                    m.TNTRun.blocksToBeRestored[blockBelowBelowThem.location] = blockBelowBelowThem.state

                    m.launchMainThread {
                        delay(400L)
                        if (!m.TNTRun.isStarted) // Event has already ended!
                            return@launchMainThread

                        blockBelowThem.type = Material.AIR
                        blockBelowBelowThem.type = Material.AIR

                        blockBelowThem.world.playSound(
                            blockBelowBelowThem.location,
                            Sound.ENTITY_CHICKEN_EGG,
                            1f,
                            DreamUtils.random.nextFloat(0.9f, 1.1f)
                        )

                        blockBelowThem.world.spawnParticle(
                            Particle.SMOKE_NORMAL,
                            blockBelowBelowThem.location,
                            5
                        )
                    }
                }
            }
        }
    }

    @EventHandler
    fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.entity.world.name == TNTRun.WORLD_NAME)
            e.isCancelled = true
    }

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (e.player in m.TNTRun.players)
            e.isCancelled = true
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (e.player.world.name != TNTRun.WORLD_NAME)
            return

        if (!(e.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || e.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
            return

        // Cancel teleport if it is chorus fruit or ender pearls
        e.isCancelled = true
    }
}