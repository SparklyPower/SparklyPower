package net.perfectdreams.dreamhome.tables

import org.jetbrains.exposed.dao.IntIdTable

object Homes : IntIdTable() {
	val owner = uuid("owner").index()
	val houseName = text("house_name")
	val worldName = text("world_name")
	val x = double("x")
	val y = double("y")
	val z = double("z")
	val yaw = float("yaw")
	val pitch = float("pitch")
	val createdAt = long("created_at")
	val editedAt = long("edited_at")
}