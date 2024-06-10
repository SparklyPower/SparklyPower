package net.perfectdreams.dreamcustomitems.utils

import net.minecraft.world.level.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.inventory.ItemStack

data class CustomBlock(
    val id: String,

    /**
     * The source item stack of this custom block, used when placing it on the ground
     */
    val sourceItemCheck: (ItemStack) -> (Boolean),

    /**
     * The target block data of this custom block
     */
    val targetBlockData: BlockData,

    /**
     * The fallback block data for clients that do not support custom blocks
     */
    val fallbackBlockData: BlockData,

    // TODO: Custom breaking speed
    // TODO: Custom drops
    val drops: () -> (List<ItemStack>)
) {
    companion object {
        val NO_DROPS: () -> (List<ItemStack>) = { emptyList() }
    }

    val targetBlockStateNMS = (targetBlockData as CraftBlockData).state
    val fallbackBlockStateNMS = (fallbackBlockData as CraftBlockData).state
    val targetBlockStateId = Block.BLOCK_STATE_REGISTRY.getId(targetBlockStateNMS)
    val fallbackBlockStateId = Block.BLOCK_STATE_REGISTRY.getId(fallbackBlockStateNMS)
}