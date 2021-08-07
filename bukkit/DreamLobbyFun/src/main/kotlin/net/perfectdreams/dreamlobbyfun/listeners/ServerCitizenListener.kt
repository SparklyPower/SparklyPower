package net.perfectdreams.dreamlobbyfun.listeners

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.citizensnpcs.api.event.NPCClickEvent
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ServerCitizenListener(val m: DreamLobbyFun) : Listener {
	@EventHandler(ignoreCancelled = false)
	fun onRightClick(e: NPCRightClickEvent) {
		onClick(e)
	}

	@EventHandler(ignoreCancelled = false)
	fun onLeftClick(e: NPCLeftClickEvent) {
		onClick(e)
	}

	fun onClick(e: NPCClickEvent) {
		// NPCClickEvent nunca é chamado (bug no Citizens?), então nós precisamos escutar por left e right clicks
		if (m.teleportToLoginLocationIfNotLoggedIn(e.clicker))
			return

		val citizenId = e.npc.id

		val serverCitizen = m.serverCitizens.firstOrNull { it.citizenId == citizenId } ?: return

		val jsonObject = JsonObject()
		jsonObject["type"] = "transferPlayer"
		jsonObject["player"] = e.clicker.name
		jsonObject["bungeeServer"] = serverCitizen.serverName

		DreamNetwork.PERFECTDREAMS_BUNGEE.sendAsync(jsonObject)
	}
}