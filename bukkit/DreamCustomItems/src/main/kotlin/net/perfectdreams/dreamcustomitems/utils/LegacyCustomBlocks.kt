package net.perfectdreams.dreamcustomitems.utils

import org.bukkit.Instrument
import org.bukkit.Material

object LegacyCustomBlocks {
    val RAINBOW_WOOL = LegacyCustomBlock(
        CustomItems.RAINBOW_WOOL,
        Material.NOTE_BLOCK,
        Instrument.DIDGERIDOO,
        1
    )

    val allCustomBlocks = listOf(
        RAINBOW_WOOL
    )
}