package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object NotifyPlayersOnline : LongIdTable() {
	val player = uuid("player")
	val tracked = uuid("tracked")
}