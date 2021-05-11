package net.perfectdreams.dreamcorreios.utils

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class Holders {
	class CorreiosHolder(val owner: Player, val correios: ContaCorreios) : InventoryHolder {
		override fun getInventory(): Inventory {
			TODO()
		}
	}

	class CorreiosMenuHolder(val owner: Player, val correios: ContaCorreios) : InventoryHolder {
		override fun getInventory(): Inventory {
			TODO()
		}
	}
}