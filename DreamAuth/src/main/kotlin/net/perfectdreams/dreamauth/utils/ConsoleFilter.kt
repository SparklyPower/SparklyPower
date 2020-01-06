package net.perfectdreams.dreamauth.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LifeCycle
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.message.Message

class ConsoleFilter : Filter {
	val sensitiveCommands = listOf(
			"/login",
			"/logar",
			"/registrar",
			"/register"
	)

	override fun getState(): LifeCycle.State {
		return LifeCycle.State.STARTED
	}

	override fun getOnMismatch(): Filter.Result? {
		return null
	}

	override fun isStopped(): Boolean {
		return false
	}

	override fun getOnMatch(): Filter.Result? {
		return null
	}

	override fun start() {}

	override fun stop() {}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, vararg p4: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?, p10: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?, p10: Any?, p11: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?, p10: Any?, p11: Any?, p12: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: String?, p4: Any?, p5: Any?, p6: Any?, p7: Any?, p8: Any?, p9: Any?, p10: Any?, p11: Any?, p12: Any?, p13: Any?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: Any?, p4: Throwable?): Filter.Result? {
		return null
	}

	override fun filter(p0: Logger?, p1: Level?, p2: Marker?, p3: Message?, p4: Throwable?): Filter.Result? {
		return null
	}

	override fun filter(event: LogEvent): Filter.Result? {
		val hasSensitive = sensitiveCommands.any { event.message.toString().toLowerCase().startsWith(it) }
		if (hasSensitive) {
			return Filter.Result.DENY
		}
		return null
	}

	override fun initialize() {}

	override fun isStarted(): Boolean {
		return true
	}
}