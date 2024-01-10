package net.perfectdreams.dreamcore.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
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

	fun askForConfirmation(sender: Player, afterAccept: (HumanEntity) -> (Unit), afterDecline: (HumanEntity) -> (Unit)) {
		val menu = createMenu(9, "§a§lConfirme a sua compra!") {
			slot(3, 0) {
				item = ItemStack(Material.GREEN_WOOL)
					.rename("§a§lQuero comprar!")

				onClick {
					afterAccept.invoke(it)
				}
			}
			slot(5, 0) {
				item = ItemStack(Material.RED_WOOL)
					.rename("§c§lTalvez outro dia...")

				onClick {
					afterDecline.invoke(it)
				}
			}
		}

		menu.sendTo(sender)
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

@Deprecated(message = "Store the items themselves on the database instead of binding to a \"Inventory\" instance")
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