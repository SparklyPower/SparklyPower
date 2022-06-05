package net.perfectdreams.dreamquickharvest.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.id.UUIDTable

object PlayerQuickHarvestData : UUIDTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.getTablePrefix()}_playerquickharvestdata"

    val energy = integer("energy")
}