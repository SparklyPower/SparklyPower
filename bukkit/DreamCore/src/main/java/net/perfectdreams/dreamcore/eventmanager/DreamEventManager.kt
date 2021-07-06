package net.perfectdreams.dreamcore.eventmanager

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamEventManager {
	val events = mutableListOf<ServerEvent>()

	fun startEventsTask() {
		scheduler().schedule(DreamCore.INSTANCE) {
			while (true) {
				val upcoming = getUpcomingEvents()

				for (event in upcoming) {
					if (event.startNow() && getRunningEvents().isEmpty()) {
						val lastestEvent = events.maxByOrNull { it.lastTime }?.lastTime

						// Only start a new event if 30s has already elapsed since the last event
						if (System.currentTimeMillis() - (lastestEvent ?: 0L) >= 30_000L)
							event.preStart()
					}
				}
				waitFor(20)
			}
		}
	}

	fun getRunningEvents(): List<ServerEvent> {
		return events.filter { it.running }
	}

	fun getUpcomingEvents(): List<ServerEvent> {
		return events.filter { !it.running }
	}

	fun addEventVictory(player: Player, eventName: String, wonAt: Long = System.currentTimeMillis())
			= addEventVictory(player.uniqueId, eventName, wonAt)
	fun addEventVictory(playerId: UUID, eventName: String, wonAt: Long = System.currentTimeMillis()) {
		DreamUtils.assertAsyncThread(true)
		transaction(Databases.databaseNetwork) {
			EventVictories.insert {
				it[EventVictories.user] = playerId
				it[EventVictories.event] = eventName
				it[EventVictories.wonAt] = wonAt
			}
		}
	}
}