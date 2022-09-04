package net.perfectdreams.dreamclubes.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Clubes : LongIdTable() {
    val name = text("name")
    val cleanName = text("clean_name")
    val shortName = text("short_name").index()
    val cleanShortName = text("clean_short_name")
    val ownerId = uuid("owner")
    val createdAt = long("created_at")
    val maxMembers = integer("max_members").default(8)
}