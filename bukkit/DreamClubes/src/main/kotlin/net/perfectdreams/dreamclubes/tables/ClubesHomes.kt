package net.perfectdreams.dreamclubes.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ClubesHomes : LongIdTable() {
    val clube = reference("clube", Clubes)
    val name = text("name")
    val worldName = text("world_name")
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
    val iconItemStack = text("icon_item_stack").nullable()

    init {
        index(true, clube, name)
    }
}