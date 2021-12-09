package net.perfectdreams.dreamclubes.tables

import net.perfectdreams.dreamclubes.tables.Clubes.nullable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Clubes : LongIdTable() {
    val name = text("name")
    val cleanName = text("clean_name")
    val shortName = text("short_name").index()
    val cleanShortName = text("clean_short_name")
    val ownerId = uuid("owner")
    val createdAt = long("created_at")
    val home = optReference("home", ClubesHomes, onDelete = ReferenceOption.CASCADE)
    val maxMembers = integer("max_members").default(8)
}