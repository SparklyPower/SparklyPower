package net.perfectdreams.dreamchat.events

import net.perfectdreams.dreamchat.utils.PlayerTag
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ApplyPlayerTagsEvent(val player: Player, val tags: MutableList<PlayerTag>) : Event(true) {
	override fun getHandlers(): HandlerList {
		return ApplyPlayerTagsEvent.handlers
	}

	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList {
			return handlers
		}
	}
}