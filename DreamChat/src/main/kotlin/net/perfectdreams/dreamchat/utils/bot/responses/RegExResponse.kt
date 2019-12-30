package net.perfectdreams.dreamchat.utils.bot.responses

import net.perfectdreams.dreamchat.utils.bot.PantufaResponse
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

open class RegExResponse : PantufaResponse {
	val regex = mutableListOf<String>()
	val patterns = mutableListOf<Pattern>()
	val response: String = "???"

	override fun handleResponse(message: String, event: AsyncPlayerChatEvent): Boolean {
		for (pattern in patterns) {
			val matcher = pattern.matcher(message)

			if (!matcher.find())
				return false
		}
		return postHandleResponse(message, event)
	}

	open fun postHandleResponse(message: String, event: AsyncPlayerChatEvent): Boolean {
		return true
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		var reply = response
		reply = reply.replace("{name}", event.player.name)
		reply = reply.replace("{displayName}", event.player.displayName)

		return reply
	}
}