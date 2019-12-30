package net.perfectdreams.dreamcore.utils

import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * Uma classe para criar menus de uma maneira simples e fácil!
 */
class DreamMenu(val size: Int, val title: String, val cancelItemMovement: Boolean, val slots: List<DreamMenuSlot>) {
	fun createInventory(): Inventory {
		val inventory = Bukkit.createInventory(DreamMenuHolder(this), size, title)
		slots.forEach {
			if (it.item != null)
				inventory.setItem(it.position, it.item)
		}
		return inventory
	}

	fun sendTo(player: Player) {
		player.openInventory(createInventory())
	}

	class DreamMenuSlot(val position: Int, val item: ItemStack?, val onClick: ((HumanEntity) -> Unit)?)

	class DreamMenuHolder(val menu: DreamMenu) : InventoryHolder {
		override fun getInventory(): Inventory {
			throw UnsupportedOperationException()
		}
	}
}

fun createMenu(size: Int, title: String, block: DreamMenuBuilder.() -> Unit) = DreamMenuBuilder(size, title).apply(block).build()

class DreamMenuBuilder(val size: Int, val title: String) {
	private val slots = mutableListOf<DreamMenu.DreamMenuSlot>()
	var cancelItemMovement: Boolean = true

	fun slot(x: Int, y: Int, block: DreamMenuSlotBuilder.() -> Unit) {
		val slot = DreamMenuSlotBuilder(x, y).apply(block).build()
		slots.add(slot)
	}

	fun build(): DreamMenu = DreamMenu(size, title, cancelItemMovement, slots)
}

class DreamMenuSlotBuilder(val x: Int, val y: Int) {
	var item: ItemStack? = null
	private var onClick: ((HumanEntity) -> Unit)? = null

	fun onClick(callback: (HumanEntity) -> Unit) {
		onClick = callback
	}

	fun build(): DreamMenu.DreamMenuSlot {
		return DreamMenu.DreamMenuSlot(x + (y * 9), item, onClick)
	}
}

class DreamMenuListener : Listener {
	@EventHandler
	fun onMove(e: InventoryClickEvent) {
		val holder = e.clickedInventory?.holder

		if (holder !is DreamMenu.DreamMenuHolder)
			return

		val dreamMenu = holder.menu

		if (dreamMenu.cancelItemMovement)
			e.isCancelled = true

		val clickedSlot = e.slot
		val slot = dreamMenu.slots.firstOrNull { it.position == clickedSlot }

		if (slot != null) {
			slot.onClick?.invoke(e.whoClicked)
		}
	}
}