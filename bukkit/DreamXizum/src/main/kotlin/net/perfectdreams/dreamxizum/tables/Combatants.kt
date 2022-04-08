package net.perfectdreams.dreamxizum.tables

import net.perfectdreams.dreamcore.DreamCore.Companion.dreamConfig
import org.jetbrains.exposed.dao.id.UUIDTable

object Combatants : UUIDTable() {
    override val tableName get() = "${dreamConfig.getTablePrefix()}_dreamxizum_combatants"
    val victories = integer("victories").default(0)
    val defeats = integer("defeats").default(0)
    val banned = bool("banned").default(false)
    val tos = bool("tos").default(false)
}