package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.tables.Bans
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Ban(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Ban>(Bans)

	var player by Bans.player
	var punishedBy by Bans.punishedBy
	var punishedAt by Bans.punishedAt
	var reason by Bans.reason
	var temporary by Bans.temporary
	var expiresAt by Bans.expiresAt
}