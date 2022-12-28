package net.perfectdreams.dreamloja.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.id.LongIdTable

object VoteSigns : LongIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.networkDatabase.tablePrefix}_votesigns"

    val owner = uuid("owner").index()
    val worldName = text("world_name")
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")
}