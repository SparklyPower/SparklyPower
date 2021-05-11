package net.perfectdreams.dreamdropparty.utils

import org.bukkit.inventory.ItemStack

class RandomItem(
    val itemStack: ItemStack,
    val chance: Double,
    val randomEnchant: Boolean = false
)