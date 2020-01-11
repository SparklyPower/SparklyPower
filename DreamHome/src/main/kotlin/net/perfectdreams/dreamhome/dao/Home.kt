package net.perfectdreams.dreamhome.dao

import net.perfectdreams.dreamhome.tables.Homes
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Home(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<Home>(Homes)

	var owner by Homes.owner
	var houseName by Homes.houseName
	var worldName by Homes.worldName
	var x by Homes.x
	var y by Homes.y
	var z by Homes.z
	var yaw by Homes.yaw
	var pitch by Homes.pitch
	var createdAt by Homes.createdAt
	var editedAt by Homes.editedAt

	fun getLocation(): Location {
		return Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
	}

	fun setLocation(location: Location) {
		worldName = location.world.name
		x = location.x
		y = location.y
		z = location.z
		yaw = location.yaw
		pitch = location.pitch
	}
}