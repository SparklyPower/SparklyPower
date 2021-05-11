package net.perfectdreams.dreamchat.dao

import net.perfectdreams.dreamchat.tables.EventMessages
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class EventMessage(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EventMessage>(EventMessages)

    var message by EventMessages.message
    var lastWinner by EventMessages.lastWinner
    var bestWinner by EventMessages.bestWinner
    var timeElapsed by EventMessages.timeElapsed
}