package net.perfectdreams.dreamchat.dao

import net.perfectdreams.dreamchat.tables.ChatUsers
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class ChatUser(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChatUser>(ChatUsers)

    var nickname by ChatUsers.nickname
    var tag by ChatUsers.tag
}