package net.perfectdreams.dreamraspadinha.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Raspadinhas : LongIdTable() {
    val receivedById = uuid("received_by").index()
    val receivedAt = long("received_at")
    val pattern = text("pattern")
    val scratched = bool("scratched")
    val value = integer("value").nullable()
}