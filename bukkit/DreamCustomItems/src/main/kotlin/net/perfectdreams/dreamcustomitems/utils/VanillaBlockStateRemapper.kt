package net.perfectdreams.dreamcustomitems.utils

import net.minecraft.world.level.block.Block
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.craftbukkit.block.data.CraftBlockData

object VanillaBlockStateRemapper {
    val TARGET_BLOCK_DEFAULT_STATE = (
            Bukkit.createBlockData(Material.TARGET) {
                it as AnaloguePowerable
                it.power = 0
            } as CraftBlockData
            ).state

    val stateToBlockState = buildMap {
        // ===[ POWERED TARGET BLOCK -> TARGET BLOCK ]===
        for (power in 1..15) {
            val targetBlockNMSState = Bukkit.createBlockData(Material.TARGET) {
                it as AnaloguePowerable
                it.power = power
            }.toNMSBlockState()

            put(targetBlockNMSState, TARGET_BLOCK_DEFAULT_STATE)
        }

        // ===[ PETRIFIED OAK SLAB -> OAK SLAB ]===
        put(
            Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.BOTTOM
            }.toNMSBlockState(),
            Bukkit.createBlockData(Material.OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.BOTTOM
            }.toNMSBlockState()
        )

        put(
            Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.TOP
            }.toNMSBlockState(),
            Bukkit.createBlockData(Material.OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.TOP
            }.toNMSBlockState()
        )

        put(
            Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.DOUBLE
            }.toNMSBlockState(),
            Bukkit.createBlockData(Material.OAK_SLAB) {
                it as Slab
                it.type = Slab.Type.DOUBLE
            }.toNMSBlockState()
        )

        // We don't store the custom blocks here because we need to process fallbacks
    }

    val stateIdToBlockState = buildMap {
        for ((originalBlockState, newBlockState) in stateToBlockState) {
            put(Block.BLOCK_STATE_REGISTRY.getId(originalBlockState), newBlockState)
        }
    }

    fun BlockData.toNMSBlockState() = (this as CraftBlockData).state
}