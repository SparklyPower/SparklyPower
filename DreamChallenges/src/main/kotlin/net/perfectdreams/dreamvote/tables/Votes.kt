package net.perfectdreams.dreamvote.tables

import org.jetbrains.exposed.dao.LongIdTable

object Votes : LongIdTable() {
    val player = uuid("player").index()
    val votedAt = long("voted_at")
    val serviceName = text("serviceName").nullable()
}