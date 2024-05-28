package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.BannedUsers
import net.perfectdreams.pantufa.tables.Profiles
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles)

	val userId = this.id.value
	var money by Profiles.money

	/**
	 * Get the user's current banned state, if it exists and if it is valid
	 */
	fun getBannedState(): ResultRow? {
		val bannedState = transaction(Databases.loritta) {
			BannedUsers.select { BannedUsers.userId eq this@Profile.id.value }
					.orderBy(BannedUsers.bannedAt, SortOrder.DESC)
					.firstOrNull()
		} ?: return null

		if (bannedState[BannedUsers.valid] && bannedState[BannedUsers.expiresAt] ?: Long.MAX_VALUE >= System.currentTimeMillis())
			return bannedState

		return null
	}
}