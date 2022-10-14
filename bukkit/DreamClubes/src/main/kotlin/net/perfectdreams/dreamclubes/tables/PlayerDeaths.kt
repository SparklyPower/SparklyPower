package net.perfectdreams.dreamclubes.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object PlayerDeaths : LongIdTable() {
    val killed = uuid("killed")
    val killer = uuid("killer").nullable()
    val time = timestampWithTimeZone("time")
}