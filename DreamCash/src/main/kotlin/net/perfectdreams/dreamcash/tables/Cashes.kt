package net.perfectdreams.dreamcash.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Cashes : IdTable<UUID>() {
    override val id: Column<EntityID<UUID>>
        get() = uniqueId
    val uniqueId = uuid("id").primaryKey().entityId()
    val cash = long("cash")
}