package net.perfectdreams.dreamchat.utils.bot

import org.bukkit.event.player.AsyncPlayerChatEvent

interface PantufaResponse {
	fun handleResponse(message: String, event: AsyncPlayerChatEvent): Boolean

	fun getResponse(message: String, event: AsyncPlayerChatEvent): String?
}