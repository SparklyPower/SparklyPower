package net.sparklypower.sparklyneonvelocity.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object GeoLocalizations : LongIdTable() {
	val ip = text("ip")
	val country = text("country")
	val region = text("region")
	val retrievedAt = long("retrieved_at")
}