package net.perfectdreams.dreamauth.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerLoggedInEvent(val player: Player) : Event(), Cancellable {
	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList {
			return handlers
		}
	}

	private var cancel = false

	override fun setCancelled(p0: Boolean) {
		cancel = p0
	}

	override fun isCancelled() = cancel

	override fun getHandlers(): HandlerList = PlayerLoggedInEvent.handlers
}