package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object EventVictories : LongIdTable() {
	val user = uuid("user").index()
	val event = text("event").index()
	val wonAt = long("won_at")
}