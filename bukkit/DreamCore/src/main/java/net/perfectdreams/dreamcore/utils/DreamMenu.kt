package net.perfectdreams.dreamcore.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * Uma classe para criar menus de uma maneira simples e fácil!
 */
class DreamMenu(val size: Int, val title: Component, val cancelItemMovement: Boolean, val slots: List<DreamMenuSlot>) {
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

fun createMenu(size: Int, title: String, block: DreamMenuBuilder.() -> Unit) = DreamMenuBuilder(size, LegacyComponentSerializer.builder().hexColors().build().deserialize(title)).apply(block).build()
fun createMenu(size: Int, title: Component, block: DreamMenuBuilder.() -> Unit) = DreamMenuBuilder(size, title).apply(block).build()

class DreamMenuBuilder(val size: Int, val title: Component) {
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

	@EventHandler
	fun onItemMove(e: InventoryMoveItemEvent) {
		// This is used to block the following:
		// Click on your inventory (the player's inventory) twice on a item
		// This drags the item from the DreamMenu to your inventory
		//
		// We also block drag from the player's inventory to DreamMenu and vice-versa
		val destinationHolder = e.destination.holder
		val sourceHolder = e.source.holder
		val dreamMenuHolder: DreamMenu.DreamMenuHolder

		// If the destination or the source is a DreamMenuHolder...
		if (destinationHolder is DreamMenu.DreamMenuHolder)
			dreamMenuHolder = destinationHolder
		else if (sourceHolder is DreamMenu.DreamMenuHolder)
			dreamMenuHolder = sourceHolder
		else return

		// The item click is already handled by InventoryClickEvent so we don't need to care about this
		val dreamMenu = dreamMenuHolder.menu

		if (dreamMenu.cancelItemMovement)
			e.isCancelled = true
	}
}