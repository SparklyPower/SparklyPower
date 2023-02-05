package net.perfectdreams.dreamsocial.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamsocial.tables.AnnouncementsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class AnnouncementsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AnnouncementsEntity>(AnnouncementsTable) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) { findById(uuid) ?: new(uuid) {} }
    }

    var firstSlot by AnnouncementsTable.firstSlot
    var secondSlot by AnnouncementsTable.secondSlot
    var thirdSlot by AnnouncementsTable.thirdSlot

    val isEmpty get() = setOf(firstSlot, secondSlot, thirdSlot).all { it == null }
}