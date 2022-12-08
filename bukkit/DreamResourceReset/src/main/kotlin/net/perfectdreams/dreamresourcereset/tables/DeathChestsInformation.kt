package net.perfectdreams.dreamresourcereset.tables

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object DeathChestsInformation : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.networkDatabase.tablePrefix}_deathchestsinformation"

    val player = reference("player", Users)
    val createdAt = timestampWithTimeZone("created_at")
    val items = text("items")
    val xp = integer("xp")
    val worldName = text("world_name").index()
    val x = integer("x").index()
    val y = integer("y").index()
    val z = integer("z").index()
    val found = bool("found")
    val foundBy = reference("found_by", Users).nullable()
    val foundAt = timestampWithTimeZone("found_at").nullable()
    val gaveBackToUser = bool("gave_back_to_user").nullable()
    val resetVersion = integer("reset_version")
}