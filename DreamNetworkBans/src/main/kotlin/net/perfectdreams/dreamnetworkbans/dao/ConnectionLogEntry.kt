package net.perfectdreams.dreamnetworkbans.dao

import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ConnectionLogEntry(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<ConnectionLogEntry>(ConnectionLogEntries)

	var player by ConnectionLogEntries.player
	var ip by ConnectionLogEntries.ip
	var connectedAt by ConnectionLogEntries.connectedAt
	var connectionStatus by ConnectionLogEntries.connectionStatus
}