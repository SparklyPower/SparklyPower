package net.perfectdreams.dreamloja.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ShopWarpUpgrades : LongIdTable() {
    val playerId = uuid("player").index()
    val boughtAt = timestampWithTimeZone("bought_at")
}