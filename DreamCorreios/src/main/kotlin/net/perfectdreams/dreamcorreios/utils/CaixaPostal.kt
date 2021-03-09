package net.perfectdreams.dreamcorreios.utils

import net.perfectdreams.dreamcore.utils.fromBase64Item
import net.perfectdreams.dreamcore.utils.toBase64
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class CaixaPostal {
	val storedItems = mutableListOf<String>()
	@Transient
	lateinit var postalItems: Inventory

	fun transformToInventory(holder: InventoryHolder? = null) {
		postalItems = Bukkit.createInventory(holder, 54, "§5§lCaixa Postal")
		postalItems.maxStackSize
		storedItems.forEach {
			postalItems.addItem(it.fromBase64Item())
		}
	}

	fun transformToBase64() {
		storedItems.clear()

		for (itemStack in postalItems.filterNotNull()) {
			storedItems.add(itemStack.toBase64())
		}
	}
}