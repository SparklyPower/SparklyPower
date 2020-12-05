package net.perfectdreams.dreamcorebungee.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Users : IdTable<UUID>() {
	override val id: Column<EntityID<UUID>> = uuid("id").primaryKey().entityId()

	val username = varchar("username", 16).index()
	val isGirl = bool("girl").default(false).index()
}