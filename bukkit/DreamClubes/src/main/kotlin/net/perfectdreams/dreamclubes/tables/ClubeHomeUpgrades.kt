package net.perfectdreams.dreamclubes.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ClubeHomeUpgrades : LongIdTable() {
    val clube = reference("clube", Clubes)
    val boughtAt = timestampWithTimeZone("bought_at")
}