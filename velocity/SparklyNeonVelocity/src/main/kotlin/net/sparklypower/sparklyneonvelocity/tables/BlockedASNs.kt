package net.sparklypower.sparklyneonvelocity.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.IdTable

object BlockedASNs : IdTable<Int>() {
	override val id = integer("asn_id").index().entityId()
	val comment = text("comment").nullable()
	val blockedAt = timestampWithTimeZone("blocked_at")
}
