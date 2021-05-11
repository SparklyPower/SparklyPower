package net.perfectdreams.dreamlobbyfun.listeners

import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class HungerListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onHunger(e: FoodLevelChangeEvent) {
		e.isCancelled = true
	}
}