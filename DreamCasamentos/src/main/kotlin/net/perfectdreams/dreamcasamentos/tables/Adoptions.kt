package net.perfectdreams.dreamcasamentos.tables

import org.jetbrains.exposed.dao.LongIdTable

object Adoptions : LongIdTable() {
    val adoptedBy = reference("adopted_by", Marriages).index()
    val player = uuid("player")
    val adotedAt = long("adopted_at")
}