package net.perfectdreams.dreamlobbyfun.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class ServerSelectorHolder : InventoryHolder {
	override fun getInventory(): Inventory? {
		return null
	}
}