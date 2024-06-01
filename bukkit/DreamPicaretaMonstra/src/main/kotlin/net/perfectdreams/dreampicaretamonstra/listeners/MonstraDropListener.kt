package net.perfectdreams.dreampicaretamonstra.listeners

import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*


class MonstraDropListener(val m: DreamPicaretaMonstra) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDropItemEvent(e: PlayerDropItemEvent) {
        // Because some people are DUMB AS FUCC
        val droppedItem = e.itemDrop.itemStack

        if (!DreamPicaretaMonstra.isMonsterTool(droppedItem))
            return

        e.itemDrop.setMetadata(
                "owner",
                FixedMetadataValue(
                        m,
                        e.player.uniqueId
                )
        )

        e.player.playSound(
                e.player.location,
                Sound.ENTITY_BLAZE_DEATH,
                1f,
                0.1f
        )

        e.player.sendTitle(
                "§cSUA MONSTRA TÁ NO CHÃO!",
                "§cPegue ela antes que ela suma!",
                10,
                140,
                10
        )

        m.logger.info("Player ${e.player.name} dropped a Picareta Monstra at ${e.player.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z}")
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDropItemEvent(e: ItemDespawnEvent) {
        // Because some people are DUMB AS FUCC
        val droppedItem = e.entity.itemStack
        if (!DreamPicaretaMonstra.isMonsterTool(droppedItem))
            return

        val metadata = e.entity.getMetadata("owner")
                .firstOrNull()

        val playerMetadata = metadata?.value() as UUID?

        if (playerMetadata != null) {
            val player = Bukkit.getPlayer(playerMetadata)

            m.logger.info("Picareta Monstra despawned at ${e.location.world.name} ${e.location.x}, ${e.location.y}, ${e.location.z} by owner ${player?.name} ${metadata}")

            if (player != null) {
                player.playSound(
                        player.location,
                        Sound.ENTITY_BLAZE_DEATH,
                        1f,
                        0.01f
                )

                player.sendTitle(
                        "§cSUA MONSTRA DESPAWNOU!",
                        "§cQuem mandou deixar ela no chão!",
                        10,
                        140,
                        10
                )
            }
        } else {
            m.logger.info("Picareta Monstra despawned at ${e.location.world.name} ${e.location.x}, ${e.location.y}, ${e.location.z} without any metadata attached")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDropItemEvent(e: PlayerItemBreakEvent) {
        // Because some people are DUMB AS FUCC
        val brokenItem = e.brokenItem
        if (!DreamPicaretaMonstra.isMonsterTool(brokenItem))
            return

        m.logger.info("Picareta Monstra broke at ${e.player.location.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z}")

        e.player.playSound(
                e.player.location,
                Sound.ENTITY_BLAZE_DEATH,
                1f,
                0.05f
        )

        e.player.sendTitle(
                "§cSUA MONSTRA QUEBROU!",
                "§cQuem mandou ficar sambando com ela na mão!",
                10,
                140,
                10
        )
    }
}