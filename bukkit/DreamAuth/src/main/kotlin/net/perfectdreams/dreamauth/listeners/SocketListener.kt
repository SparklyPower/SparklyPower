package net.perfectdreams.dreamauth.listeners

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.events.PlayerLoggedInEvent
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SocketListener(val m: DreamAuth) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onSocket(e: SocketReceivedEvent) {
		val obj = e.json.obj
		m.logger.info("Received socket event!")
		val type = obj["type"].nullString
		m.logger.info("$type")

		val removeFromList = type == "removeFromPremiumPlayerList"
		val addToList = type == "addToPremiumPlayerList"
		val uniqueId = obj["uniqueId"].nullString ?: return

		val uuid = UUID.fromString(uniqueId)
		if (addToList) {
			m.logger.info("Adding $uuid as premium user!")
			m.premiumUsers.add(uuid)
		} else if (removeFromList) {
			m.logger.info("Removing $uuid from the premium users list!")
			m.premiumUsers.remove(uuid)
		}
	}
}
