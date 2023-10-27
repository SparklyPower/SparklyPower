package net.sparklypower.sparklyneonvelocity.dao

import net.sparklypower.sparklyneonvelocity.tables.IpBans
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class IpBan(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<IpBan>(IpBans)

	var ip by IpBans.ip

	var punishedBy by IpBans.punishedBy
	var punishedAt by IpBans.punishedAt
	var reason by IpBans.reason
	var temporary by IpBans.temporary
	var expiresAt by IpBans.expiresAt
}