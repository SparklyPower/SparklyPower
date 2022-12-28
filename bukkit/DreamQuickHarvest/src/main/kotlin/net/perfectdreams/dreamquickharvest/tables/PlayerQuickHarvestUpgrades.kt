package net.perfectdreams.dreamquickharvest.tables

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object PlayerQuickHarvestUpgrades : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.networkDatabase.tablePrefix}_playerquickharvestupgrades"

    val playerId = uuid("player").index()
    val energy = integer("energy")
    val boughtAt = timestampWithTimeZone("bought_at")
    val expiresAt = timestampWithTimeZone("expires_at").index()
}