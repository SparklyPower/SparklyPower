package net.perfectdreams.dreaminventorysnapshots.tables

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.exposed.jsonb
import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object InventorySnapshots : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.networkDatabase.tablePrefix}_inventorysnapshots"

    val playerId = uuid("player").index()
    val createdAt = timestampWithTimeZone("created_at").index()
    val content = jsonb("content")
}