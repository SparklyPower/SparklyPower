package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Cashes : IdTable<UUID>() {
    override val id: Column<EntityID<UUID>>
        get() = uniqueId
    val uniqueId = uuid("id").entityId()
    val cash = long("cash")

    override val primaryKey = PrimaryKey(uniqueId)
}