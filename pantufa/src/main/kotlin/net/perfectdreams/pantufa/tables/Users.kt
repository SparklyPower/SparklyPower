package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Users : IdTable<UUID>() {
    override val id: Column<EntityID<UUID>> = uuid("id").entityId()

    val username = varchar("username", 16).index()
    val isGirl = bool("girl").default(false).index()

    override val primaryKey = PrimaryKey(id)
}