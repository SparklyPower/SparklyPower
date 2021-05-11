package net.perfectdreams.dreamcustomitems.utils

import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class CustomBlock(
    val sourceItem: ItemStack,
    val targetType: Material,
    val instrument: Instrument,
    val note: Int
)