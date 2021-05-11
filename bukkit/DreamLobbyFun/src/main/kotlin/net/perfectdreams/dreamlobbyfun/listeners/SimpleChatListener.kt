package net.perfectdreams.dreamlobbyfun.listeners

import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class SimpleChatListener(val m: DreamLobbyFun) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onChat(e: AsyncPlayerChatEvent) {
		e.isCancelled = true

		Bukkit.broadcastMessage("§7${e.player.name} §6§l» §7${e.message}")
	}
}