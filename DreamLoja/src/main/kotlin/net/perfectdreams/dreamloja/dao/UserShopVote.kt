package net.perfectdreams.dreamloja.dao

import net.perfectdreams.dreamloja.tables.UserShopVotes
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class UserShopVote(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<UserShopVote>(UserShopVotes)

	var givenBy by UserShopVotes.givenBy
	var receivedBy by UserShopVotes.receivedBy
	var receivedAt by UserShopVotes.receivedAt
}