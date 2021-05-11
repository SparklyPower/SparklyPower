package net.perfectdreams.dreamnetworkbans.dao

import net.perfectdreams.dreamnetworkbans.tables.Fingerprints
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Fingerprint(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Fingerprint>(Fingerprints)

	var player by Fingerprints.player
	var createdAt by Fingerprints.createdAt
	var isForgeUser by Fingerprints.isForgeUser
	var chatMode by Fingerprints.chatMode
	var mainHand by Fingerprints.mainHand
	var language by Fingerprints.language
	var viewDistance by Fingerprints.viewDistance
	var version by Fingerprints.version

	var hasCape by Fingerprints.hasCape
	var hasHat by Fingerprints.hasHat
	var hasJacket by Fingerprints.hasJacket
	var hasLeftPants by Fingerprints.hasLeftPants
	var hasLeftSleeve by Fingerprints.hasLeftSleeve
	var hasRightPants by Fingerprints.hasRightPants
	var hasRightSleeve by Fingerprints.hasRightSleeve
}