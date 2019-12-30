package net.perfectdreams.dreamcore.eventmanager

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.scheduler

class DreamEventManager {
	val events = mutableListOf<ServerEvent>()

	fun startEventsTask() {
		scheduler().schedule(DreamCore.INSTANCE) {
			while (true) {
				val upcoming = getUpcomingEvents()

				for (event in upcoming) {
					if (event.startNow()) {
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
}