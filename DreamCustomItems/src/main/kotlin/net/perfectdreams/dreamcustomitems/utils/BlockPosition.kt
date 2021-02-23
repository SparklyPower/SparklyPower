package net.perfectdreams.dreamcustomitems.utils

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

data class BlockPosition(
    val x: Int,
    val y: Int,
    val z: Int
) {
    companion object {
        fun fromBlock(block: Block) = fromLocation(block.location)
        fun fromLocation(location: Location) = BlockPosition(location.blockX, location.blockY, location.blockZ)
    }
}