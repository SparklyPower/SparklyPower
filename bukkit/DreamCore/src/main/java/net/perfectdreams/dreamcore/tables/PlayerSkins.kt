package net.perfectdreams.dreamcore.tables

import net.perfectdreams.dreamcore.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.UUIDTable

object PlayerSkins : UUIDTable() {
    val data = jsonb("data")
}