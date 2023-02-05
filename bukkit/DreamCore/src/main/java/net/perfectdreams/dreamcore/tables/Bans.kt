package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Bans : LongIdTable() {
    val player = uuid("player").index()

    val punishedBy = uuid("punished_by").nullable()
    val punishedAt = long("punished_at")

    val reason = text("reason").nullable()
    val temporary = bool("temporary").default(false)
    val expiresAt = long("expires_at").nullable()
}