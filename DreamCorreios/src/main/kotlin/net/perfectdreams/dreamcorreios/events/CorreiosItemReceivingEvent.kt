package net.perfectdreams.dreamcorreios.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class CorreiosItemReceivingEvent(var player: Player? = null, var itemStacks: Array<ItemStack> = arrayOf<ItemStack>()) : Event(), Cancellable {
	private var cancelled = false
	var giveToPlayer = true
	var sendToCorreios = true

	override fun setCancelled(p0: Boolean) {
		cancelled = p0
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}

	override fun getHandlers(): HandlerList = Companion.handlers

	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList = handlers
	}
}