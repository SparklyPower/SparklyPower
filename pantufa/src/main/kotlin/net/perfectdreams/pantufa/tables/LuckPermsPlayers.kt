package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.sql.Table

object LuckPermsPlayers : Table("luckperms_players") {
    val id = varchar("uuid", 36)
    val username = varchar("username", 16)
    val primaryGroup = varchar("primary_group", 36)
}