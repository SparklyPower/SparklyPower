package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BannedUsers : LongIdTable() {
	val userId = long("user").index()
	val valid = bool("valid").index()
	val bannedAt = long("banned_at")
	val expiresAt = long("expires_at").nullable()
	val reason = text("reason")
	val bannedBy = long("banned_by").nullable()
}