package net.perfectdreams.dreamsocial.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object AnnouncementsTable : UUIDTable() {
    val firstSlot = text("first_slot").nullable()
    val secondSlot = text("second_slot").nullable()
    val thirdSlot = text("third_slot").nullable()
}