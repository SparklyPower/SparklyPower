package net.perfectdreams.dreamresourcereset.tables

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamresourcereset.utils.DeathChestMapCharacter
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import org.jetbrains.exposed.dao.id.IntIdTable

object DeathChestMaps : IntIdTable() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.networkDatabase.tablePrefix}_deathchestmaps"

    val chest = reference("chest", DeathChestsInformation)
    val character = postgresEnumeration<DeathChestMapCharacter>("character")
}