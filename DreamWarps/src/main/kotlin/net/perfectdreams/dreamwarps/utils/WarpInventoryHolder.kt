package net.perfectdreams.dreamwarps.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class WarpInventoryHolder : InventoryHolder {
	override fun getInventory(): Inventory {
		throw RuntimeException()
	}
}