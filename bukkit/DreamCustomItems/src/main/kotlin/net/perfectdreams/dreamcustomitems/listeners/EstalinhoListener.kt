package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.*
import org.bukkit.block.data.Levelled
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class EstalinhoListener(val m: DreamCustomItems) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return
        val heldItem = e.item ?: return

        if (heldItem.type == Material.PRISMARINE_SHARD && heldItem.hasItemMeta() && (heldItem.itemMeta.customModelData == 2 || heldItem.itemMeta.customModelData == 3)) {
            // is biribinha, yay!
            val particleSpawnLocation = clickedBlock.location.toCenterLocation().add(0.0, 0.6, 0.0)

            val dustOptions = Particle.DustOptions(
                when (heldItem.itemMeta.customModelData) {
                    2 -> Color.RED
                    3 -> Color.GREEN
                    else -> Color.BLACK
                },
                2.0f
            )

            clickedBlock.world.playSound(
                particleSpawnLocation,
                Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                1.0f,
                DreamUtils.random.nextDouble(1.2, 2.0).toFloat()
            )

            clickedBlock.world.spawnParticle(
                Particle.CRIT,
                particleSpawnLocation,
                5,
                0.0,
                0.0,
                0.0,
                1.0
            )

            clickedBlock.world.spawnParticle(
                Particle.REDSTONE,
                particleSpawnLocation,
                5,
                0.3,
                0.0,
                0.3,
                1.0,
                dustOptions
            )

            // We need to decrease it only after spawning the particle
            // because if we decrease it before, and the new amount is 0, the item meta will be null!
            heldItem.amount -= 1 // Decrease one
        }
    }
}