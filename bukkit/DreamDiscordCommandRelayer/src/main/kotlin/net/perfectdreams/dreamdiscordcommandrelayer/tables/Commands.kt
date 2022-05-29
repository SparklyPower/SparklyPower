package net.perfectdreams.dreamdiscordcommandrelayer.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Commands : LongIdTable() {
    override val tableName: String get() = "survival_commands"
    val player = varchar("player", 16).index()
    val world = varchar("world", 64).index()
    val alias = varchar("alias", 64).index()
    val args = text("args").nullable()
    val time = long("time")
    val x = double("x")
    val y = double("y")
    val z = double("z")
}