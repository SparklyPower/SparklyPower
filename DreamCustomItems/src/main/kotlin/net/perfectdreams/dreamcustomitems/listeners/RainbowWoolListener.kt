package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Material
import org.bukkit.block.data.MultipleFacing
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class RainbowWoolListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (e.itemInHand.type == Material.WHITE_WOOL && e.itemInHand.hasItemMeta() && e.itemInHand.itemMeta.customModelData == 1) {
            e.block.type = Material.BROWN_MUSHROOM_BLOCK
            val blockData = e.block.blockData as MultipleFacing
            blockData.allowedFaces.forEach {
                blockData.setFace(it, false)
            }
            e.block.blockData = blockData
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        if (e.block.type == Material.BROWN_MUSHROOM_BLOCK) {
            val blockData = e.block.blockData as MultipleFacing
            if (blockData.allowedFaces.all { !blockData.hasFace(it) }) {
                e.isCancelled = true
                e.block.type = Material.AIR
                e.block.world.dropItem(
                    e.block.location.toCenterLocation(),
                    CustomItems.RAINBOW_WOOL
                )
            }
        }
    }
}