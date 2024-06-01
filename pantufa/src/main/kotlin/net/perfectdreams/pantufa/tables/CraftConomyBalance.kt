package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.sql.Table

object CraftConomyBalance : Table("cc3_balance") {
    val balance = double("balance")
    val id = integer("username_id")
}