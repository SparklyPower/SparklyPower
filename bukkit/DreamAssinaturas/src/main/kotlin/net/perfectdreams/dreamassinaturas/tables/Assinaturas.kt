package net.perfectdreams.dreamassinaturas.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import java.util.*

object Assinaturas : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.getTablePrefix()}_assinaturas"

    val signedBy = uuid("signed_by").index()
    val signedAt = long("signed_at")
    val worldName = text("world_name")

    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
}