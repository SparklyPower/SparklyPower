package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.sql.Table

object LuckPermsUserPermissions : Table("luckperms_user_permissions") {
    val uuid = varchar("uuid", 36)
    val permission = varchar("permission", 200)
    val expiry = integer("expiry")
}