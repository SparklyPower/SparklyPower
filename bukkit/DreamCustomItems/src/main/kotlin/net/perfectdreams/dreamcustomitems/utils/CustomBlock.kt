package net.perfectdreams.dreamcustomitems.utils

import net.minecraft.world.level.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.inventory.ItemStack

data class CustomBlock(
    val id: String,

    /**
     * The source item stack of this custom block, used when placing it on the ground
     */
    val sourceItemCheck: (ItemStack) -> (Boolean),

    val blockStates: List<CustomBlockState>
    // TODO: Custom breaking speed
) {
    companion object {
        val NO_DROPS: () -> (List<ItemStack>) = { emptyList() }
    }

    data class CustomBlockState(
        /**
         * The source block data on the server
         */
        val sourceBlockData: BlockData,

        /**
         * The target block data of this custom block
         */
        val targetBlockData: BlockData,

        /**
         * The fallback block data for clients that do not support custom blocks
         */
        val fallbackBlockData: BlockData,

        val drops: () -> (List<ItemStack>)
    ) {
        val sourceBlockState = sourceBlockData.createBlockState()
        val sourceBlockStateNMS = (sourceBlockState as CraftBlockState).handle
        val sourceBlockStateId = Block.BLOCK_STATE_REGISTRY.getId(sourceBlockStateNMS)
        val targetBlockStateNMS = (targetBlockData as CraftBlockData).state
        val fallbackBlockStateNMS = (fallbackBlockData as CraftBlockData).state
        val targetBlockStateId = Block.BLOCK_STATE_REGISTRY.getId(targetBlockStateNMS)
        val fallbackBlockStateId = Block.BLOCK_STATE_REGISTRY.getId(fallbackBlockStateNMS)
    }
}