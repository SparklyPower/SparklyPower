package net.perfectdreams.dreamchat.utils.chatevent

import net.perfectdreams.dreamchat.dao.EventMessage
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.broadcastMessage
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class EventoChatMensagem : IEventoChat {
	lateinit var currentMessage: EventMessage
	val messages = mutableListOf<EventMessage>()

	fun loadDatabaseMessages() {
		transaction(Databases.databaseNetwork) {
			messages.addAll(EventMessage.all())
		}
	}

	override fun preStart() {
		currentMessage = messages.random()
	}

	override fun postEndAsync(winner: Player, timeElapsed: Long) {
		transaction(Databases.databaseNetwork) {
			currentMessage.lastWinner = winner.uniqueId
			val lastRecord = currentMessage.timeElapsed

			if (lastRecord == null || lastRecord > timeElapsed) {
				currentMessage.bestWinner = winner.uniqueId
				currentMessage.timeElapsed = timeElapsed
			}
		}
	}

	override fun sendWinnerMessages(winner: Player, timeElapsed: Long) {
		super.sendWinnerMessages(winner, timeElapsed)

		val lastRecord = currentMessage.timeElapsed

		if (lastRecord != null && lastRecord > timeElapsed) {
			// Novo recorde, woo!
			val bestWinner = currentMessage.bestWinner
			if (bestWinner != null) {
				broadcastMessage(BroadcastType.CHAT_EVENT) {
					"§c(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ §aNOVO RECORDE! §c✧ﾟ･: *ヽ(◕ヮ◕ヽ)".centralize()
				}

				val player = Bukkit.getOfflinePlayer(bestWinner)

				val faster = lastRecord.toDouble() / timeElapsed.toDouble()
				broadcastMessage(BroadcastType.CHAT_EVENT) {
					"§b${winner.displayName}§r§a bateu o recorde de §b${player?.name
						?: "???"}§r§a! §3(${faster}x mais rápido!)".centralize()
				}

				val seconds = lastRecord / 1000L
				val milli = lastRecord % 1000L

				broadcastMessage(BroadcastType.CHAT_EVENT) {
					("§6Antigo recorde: §e$seconds segundos§6 e §e$milli milissegundos§6!").centralize()
				}
			}
		}
	}

	override fun getAnnouncementMessage() = currentMessage.message

	override fun getToDoWhat(): String {
		return "escrever"
	}

	@Synchronized
	override fun process(player: Player, message: String): Boolean {
		val contains = message.equals(currentMessage.message, true)

		if (contains)
			return true

		if (!contains) {
			val distance = StringUtils.getLevenshteinDistance(message, currentMessage.message)

			if (10 >= distance) {
				player.playSound(player.location, "perfectdreams.sfx.errou", 1f, 1f)
			}
		}

		return false
	}
}