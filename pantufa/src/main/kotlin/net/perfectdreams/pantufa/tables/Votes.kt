package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Votes : LongIdTable() {
    val player = uuid("player").index()
    val votedAt = long("voted_at")
    val serviceName = text("serviceName").nullable()
}