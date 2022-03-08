package net.perfectdreams.dreamcore.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream

object InventoryUtils {
	/**
	 * Limpa todo o inventário, incluindo a armadura
	 *
	 * @param inventory o inventário que será limpado
	 */
	fun clearEverything(inventory: PlayerInventory) {
		inventory.clear()
		inventory.helmet = null
		inventory.chestplate = null
		inventory.leggings = null
		inventory.boots = null
	}
}

fun Inventory.canHoldItem(stack: ItemStack?): Boolean {
	if (stack == null) { return true }
	if (stack.type == Material.AIR) { return true }
	val storageContents = this.storageContents
	if (storageContents != null) {
		for (invItem in storageContents) {
			if (invItem == null) return true // Slot vazio!
			if (invItem.isSimilar(stack)) { // Itens parecidos
				if (invItem.maxStackSize >= invItem.amount + stack.amount) {
					// Sim, dá para adicionar itens e ainda ter espaço vazio!
					return true
				}
			}
		}
	}
	return false
}

fun Inventory.toBase64(i: Int): String {
	val outputStream = ByteArrayOutputStream()
	try {
		val dataOutput = BukkitObjectOutputStream(outputStream as OutputStream)
		dataOutput.writeInt(i)
		dataOutput.writeInt(this.size)
		if (i == 1) {
			dataOutput.writeUTF("???")
		}
		val contents = this.contents ?: error("Inventory contents is null!")
		for ((index, itemStack) in contents.withIndex()) {
			if (itemStack != null) {
				dataOutput.writeObject(itemStack.toBase64())
			} else {
				dataOutput.writeObject(null)
			}
			dataOutput.writeInt(index)
		}
		dataOutput.close()
		return Base64Coder.encodeLines(outputStream.toByteArray())
	} catch (e: Exception) {
		throw IllegalStateException("Unable to save item stacks.", e)
	}

}

fun String.fromBase64Inventory(): Inventory {
	try {
		val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(this))
		val dataInput = BukkitObjectInputStream(inputStream)
		val type = dataInput.readInt()
		val size = dataInput.readInt()
		val inv = if (type == 0) {
			Bukkit.createInventory(null as InventoryHolder?, size)
		} else {
			Bukkit.createInventory(null as InventoryHolder?, size, dataInput.readUTF())
		}
		for (i in 0 until inv!!.size) {
			val utf = dataInput.readObject()
			val slot = dataInput.readInt()
			if (utf != null) {
				inv.setItem(slot, (utf as String).fromBase64Item())
			}
		}
		dataInput.close()
		return inv
	} catch (e: Exception) {
		throw IllegalStateException("Unable to load item stacks.", e)
	}

}