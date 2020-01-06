package net.perfectdreams.dreamcorebungee.dao

import net.perfectdreams.dreamcorebungee.tables.Users
import org.jetbrains.exposed.dao.*
import java.util.*

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var isGirl by Users.isGirl
}