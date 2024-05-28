package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.sql.Table

object LuckPermsGroupPermissions : Table("luckperms_group_permissions") {
    val name = varchar("name", 36)
    val permission = varchar("permission", 200)
    val expiry = integer("expiry")
}