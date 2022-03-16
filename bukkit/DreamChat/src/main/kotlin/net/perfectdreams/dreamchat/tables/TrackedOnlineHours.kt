package net.perfectdreams.dreamchat.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.id.LongIdTable
import net.perfectdreams.dreamchat.utils.exposed.timestampWithTimeZone

object TrackedOnlineHours : LongIdTable() {
	override val tableName: String
		get() = "${DreamCore.dreamConfig.getTablePrefix()}_trackedonlinehours"

	val player = uuid("player").index()
	val loggedIn = timestampWithTimeZone("logged_in").index()
	val loggedOut = timestampWithTimeZone("logged_out")
}