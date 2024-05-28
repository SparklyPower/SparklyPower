package net.perfectdreams.dreamsonecas.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

object PlayerSonecas : IdTable<UUID>() {
    override val id = uuid("player_id").entityId()
    override val primaryKey = PrimaryKey(id)

    // Support up to max long!
    val money = decimal("money", 21, 2).index()
    val updatedAt = timestampWithTimeZone("updated_at")
}