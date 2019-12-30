package net.perfectdreams.dreamvote.dao

import net.perfectdreams.dreamvote.tables.Votes
import org.jetbrains.exposed.dao.*

class Vote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(Votes)

    var player by Votes.player
    var votedAt by Votes.votedAt
    var website by Votes.serviceName
}