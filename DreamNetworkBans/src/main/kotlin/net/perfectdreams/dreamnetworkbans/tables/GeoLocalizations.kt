package net.perfectdreams.dreamnetworkbans.tables

import org.jetbrains.exposed.dao.LongIdTable

object GeoLocalizations : LongIdTable() {
	val ip = text("ip")
	val country = text("country")
	val region = text("region")
	val retrievedAt = long("retrieved_at")
}