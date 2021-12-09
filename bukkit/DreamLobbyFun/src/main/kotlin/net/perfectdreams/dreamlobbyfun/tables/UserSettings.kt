package net.perfectdreams.dreamlobbyfun.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object UserSettings : UUIDTable() {
	val playerVisibility = bool("player_visibility")
}