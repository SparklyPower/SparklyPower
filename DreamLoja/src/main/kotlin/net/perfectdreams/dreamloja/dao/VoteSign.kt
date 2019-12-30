package net.perfectdreams.dreamloja.dao

import net.perfectdreams.dreamloja.tables.VoteSigns
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class VoteSign(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<VoteSign>(VoteSigns)

	var owner by VoteSigns.owner
	var worldName by VoteSigns.worldName
	var x by VoteSigns.x
	var y by VoteSigns.y
	var z by VoteSigns.z
	var yaw by VoteSigns.yaw
	var pitch by VoteSigns.pitch

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