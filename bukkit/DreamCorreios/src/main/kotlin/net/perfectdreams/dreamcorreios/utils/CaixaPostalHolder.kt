package net.perfectdreams.dreamcorreios.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class CaixaPostalHolder(
    val caixaPostalAccessHolder: CaixaPostalAccessHolder,
    val itemsPerPages: List<MutableList<ItemStack>>,
    val page: Int
) : InventoryHolder {
    override fun getInventory(): Inventory {
        TODO("Not yet implemented")
    }
}