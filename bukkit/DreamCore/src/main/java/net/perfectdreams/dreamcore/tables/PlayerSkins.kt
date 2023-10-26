package net.perfectdreams.dreamcore.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.UUIDTable

object PlayerSkins : UUIDTable() {
    val data = jsonb("data")
}