package net.perfectdreams.dreamcore.network.socket

import com.google.gson.JsonObject
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class SocketReceivedEvent(val json: JsonObject, var response: JsonObject) : Event(true) {
	override fun getHandlers(): HandlerList {
		return SocketReceivedEvent.handlers
	}

	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList {
			return handlers
		}
	}
}