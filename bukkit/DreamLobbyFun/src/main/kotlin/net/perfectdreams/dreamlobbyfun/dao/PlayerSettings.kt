package net.perfectdreams.dreamlobbyfun.dao

import net.perfectdreams.dreamlobbyfun.tables.UserSettings
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class PlayerSettings(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<PlayerSettings>(UserSettings)

	var playerVisibility by UserSettings.playerVisibility
}