package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.sql.Table

object CraftConomyAccounts : Table("cc3_account") {
    val id = integer("id")
    val name = varchar("name", 50)
    val uuid = varchar("uuid", 36).nullable()
}