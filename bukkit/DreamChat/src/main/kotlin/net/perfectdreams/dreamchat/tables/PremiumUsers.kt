package net.perfectdreams.dreamchat.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PremiumUsers : LongIdTable() {
	val premiumUniqueId = uuid("premium_uuid").index()
	val crackedUniqueId = uuid("cracked_uuid").index()
	val crackedUsername = text("cracked_username")
}
