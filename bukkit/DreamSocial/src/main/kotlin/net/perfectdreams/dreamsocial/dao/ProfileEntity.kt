package net.perfectdreams.dreamsocial.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamsocial.tables.ProfilesTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ProfileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileEntity>(ProfilesTable) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) { findById(uuid) ?: new(uuid) {} }
    }

    var layout by ProfilesTable.layout
}