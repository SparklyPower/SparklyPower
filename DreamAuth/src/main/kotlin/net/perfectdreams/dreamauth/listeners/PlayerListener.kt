package net.perfectdreams.dreamauth.listeners

import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.utils.extensions.displaced
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

class PlayerListener(val m: DreamAuth) : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onMove(e: PlayerMoveEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		if (e.displaced)
			e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onItemHeld(e: PlayerItemHeldEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onInteract(e: PlayerInteractEntityEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onDropItem(e: PlayerDropItemEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onInteract(e: PlayerInteractEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClick(e: InventoryClickEvent) {
		val isLoggedIn = m.playerStatus[e.whoClicked] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onAsyncChat(e: AsyncPlayerChatEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		val isLoggedIn = m.playerStatus[e.player] == PlayerStatus.LOGGED_IN

		if (isLoggedIn)
			return

		val command = e.message.split(" ").getOrNull(0)?.toLowerCase() ?: return

		if (!DreamAuth.WHITELISTED_COMMANDS.contains(command))
			e.isCancelled = true
	}
}