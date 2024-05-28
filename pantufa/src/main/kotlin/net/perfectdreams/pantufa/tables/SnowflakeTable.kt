package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class SnowflakeTable(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
	override val id: Column<EntityID<Long>> = long(columnName).entityId()
	override val primaryKey = PrimaryKey(id)
}

abstract class SnowflakeEntity(id: EntityID<Long>) : Entity<Long>(id)

abstract class SlowflakeEntityClass<out E: LongEntity>(table: IdTable<Long>, entityType: Class<E>? = null) : EntityClass<Long, E>(table, entityType)
