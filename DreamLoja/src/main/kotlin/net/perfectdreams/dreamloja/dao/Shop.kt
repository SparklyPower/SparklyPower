package net.perfectdreams.dreamloja.dao

import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Shop(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Shop>(Shops)

	var owner by Shops.owner
	var shopName by Shops.shopName
	var worldName by Shops.worldName
	var x by Shops.x
	var y by Shops.y
	var z by Shops.z
	var yaw by Shops.yaw
	var pitch by Shops.pitch
	var iconItemStack by Shops.iconItemStack

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