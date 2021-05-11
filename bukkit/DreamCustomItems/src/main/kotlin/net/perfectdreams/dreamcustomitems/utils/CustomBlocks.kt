package net.perfectdreams.dreamcustomitems.utils

import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace

object CustomBlocks {
    val RAINBOW_WOOL = CustomBlock(
        CustomItems.RAINBOW_WOOL,
        Material.NOTE_BLOCK,
        Instrument.DIDGERIDOO,
        1
    )

    val allCustomBlocks = listOf(
        RAINBOW_WOOL
    )
}