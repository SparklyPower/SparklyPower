package net.perfectdreams.dreammapwatermarker.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsGeneratedMaps : LongIdTable() {
    val album = long("album_id").index()
    val sticker = long("sticker_id").index()
    val map = integer("map_id")
    val generatedAt = timestampWithTimeZone("generated_at")
}