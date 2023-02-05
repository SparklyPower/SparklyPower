package net.perfectdreams.dreamcasamentos.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Marriages : LongIdTable() {
    val player1 = uuid("player1").index()
    val player2 = uuid("player2").index()

    val homeWorld = text("home_world").nullable()
    val homeX = double("home_x").nullable()
    val homeY = double("home_y").nullable()
    val homeZ = double("home_z").nullable()

    val marriedAt = long("married_at").clientDefault(System::currentTimeMillis).nullable()
}