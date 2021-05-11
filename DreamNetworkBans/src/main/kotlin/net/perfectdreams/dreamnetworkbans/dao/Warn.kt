package net.perfectdreams.dreamnetworkbans.dao

import net.perfectdreams.dreamnetworkbans.tables.Warns
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Warn(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Warn>(Warns)

	var player by Warns.player
	var punishedBy by Warns.punishedBy
	var punishedAt by Warns.punishedAt
	var reason by Warns.reason
}