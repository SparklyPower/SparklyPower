package net.perfectdreams.dreamclubes.tables

import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object ClubeMembers : IdTable<UUID>() {
    override val id: Column<EntityID<UUID>> = uuid("id").primaryKey().entityId()

    val clube = reference("clube", Clubes)
    val permissionLevel = enumeration("permission_level", ClubePermissionLevel::class)
    val customPrefix = text("custom_prefix").nullable()
}