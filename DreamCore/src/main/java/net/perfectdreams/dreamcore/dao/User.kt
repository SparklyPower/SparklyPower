package net.perfectdreams.dreamcore.dao

import net.perfectdreams.dreamcore.tables.Users
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class User(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<User>(Users)

	var username by Users.username
	var isGirl by Users.isGirl
}