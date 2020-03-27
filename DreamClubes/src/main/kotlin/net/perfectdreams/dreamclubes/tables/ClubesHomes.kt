package net.perfectdreams.dreamclubes.tables

import org.jetbrains.exposed.dao.LongIdTable

object ClubesHomes : LongIdTable() {
    val worldName = text("world_name")
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
}