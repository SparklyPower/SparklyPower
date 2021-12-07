package net.perfectdreams.dreamcorreios.utils

import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.libs.org.bson.codecs.pojo.annotations.BsonCreator
import net.perfectdreams.libs.org.bson.codecs.pojo.annotations.BsonIgnore
import net.perfectdreams.libs.org.bson.codecs.pojo.annotations.BsonProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ContaCorreios @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonIgnore]
		val username: String) {
	var caixasPostais = mutableListOf<CaixaPostal>()

	constructor() : this("???")

	fun addItems(player: Player?, giveToPlayer: Boolean, sendMessage: Boolean, vararg _itemStacks: ItemStack) {
		val caixasSubstitutas = mutableListOf<CaixaPostal>()
		var nullItemStacks = mutableListOf<ItemStack?>()
		val fromPostalBox = mutableListOf<ItemStack?>()

		caixasPostais.forEach {
			it.transformToInventory()
			nullItemStacks.addAll(it.postalItems.contents!!)
			fromPostalBox.addAll(it.postalItems.contents!!)
		}

		nullItemStacks.addAll(_itemStacks)

		val itemStacks = nullItemStacks.filterNotNull().toMutableList()

		while (itemStacks.isNotEmpty()) {
			val toRemove = mutableListOf<ItemStack>()

			if (giveToPlayer && player != null) {
				for (itemStack in itemStacks) {
					if (fromPostalBox.contains(itemStack))
						continue

					if (player.inventory.canHoldItem(itemStack)) {
						player.inventory.addItem(itemStack)
						toRemove.add(itemStack)
					}
				}
			}

			caixasSubstitutas.forEach {
				for (itemStack in itemStacks) {
					if (it.postalItems.canHoldItem(itemStack)) {
						it.postalItems.addItem(itemStack)
						toRemove.add(itemStack)
					}
				}
			}

			itemStacks.removeAll(toRemove)

			if (itemStacks.isNotEmpty()) {
				// gerson traz o litrao pq a caixa postal ta cabendo nao https://twitter.com/Luca02015/status/923606863882616832
				val caixaPostal = CaixaPostal()
				caixaPostal.transformToInventory() // Iniciar o inventário
				caixasSubstitutas.add(caixaPostal)
			}
		}

		caixasPostais = caixasSubstitutas

		caixasPostais.forEach {
			it.transformToBase64()
		}
	}
}