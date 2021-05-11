package net.perfectdreams.dreamkits.utils

import org.bukkit.inventory.ItemStack

class Kit {
	var name: String = "???"
	var fancyName: String = "???"
	var items = mutableListOf<ItemStack>()
	var delay: Long = 0L
	var giveOnFirstJoin: Boolean = false
}