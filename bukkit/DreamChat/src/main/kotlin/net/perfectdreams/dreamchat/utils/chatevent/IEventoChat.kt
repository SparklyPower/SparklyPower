package net.perfectdreams.dreamchat.utils.chatevent

import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.broadcastMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

interface IEventoChat {
	fun preStart() {}
	fun postEnd() {}
	fun postEndAsync(winner: Player, timeElapsed: Long) {}

	fun sendWinnerMessages(winner: Player, timeElapsed: Long) {
		broadcastMessage(BroadcastType.CHAT_EVENT) { ("§6Parabéns §e" + winner.getDisplayName() + "§6!").centralize() }
		val seconds = timeElapsed / 1000L
		val milli = timeElapsed % 1000L
		broadcastMessage(BroadcastType.CHAT_EVENT) { ("§6Tempo gasto: §e$seconds segundos§6 e §e$milli milissegundos§6!").centralize() }
	}

	fun getAnnouncementMessage(): String
	fun getToDoWhat(): String
	fun process(player: Player, message: String): Boolean
}