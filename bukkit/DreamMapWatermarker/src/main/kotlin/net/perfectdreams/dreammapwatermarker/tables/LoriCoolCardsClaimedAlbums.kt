package net.perfectdreams.dreammapwatermarker.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsClaimedAlbums : LongIdTable() {
    val claimedBy = uuid("claimed_by").index()
    val albumId = long("album_id").index()
    val finishedId = long("finished_id").index()
    val claimedAt = timestampWithTimeZone("claimed_at")
}