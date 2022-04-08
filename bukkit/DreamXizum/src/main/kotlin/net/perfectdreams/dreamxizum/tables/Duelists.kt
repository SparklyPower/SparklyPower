package net.perfectdreams.dreamxizum.tables

import net.perfectdreams.dreamcore.DreamCore.Companion.dreamConfig
import org.jetbrains.exposed.dao.id.IntIdTable

object Duelists : IntIdTable() {
    override val tableName get() = "${dreamConfig.getTablePrefix()}_dreamxizum_duelists"
    val uuid = uuid("uuid").index()
    val season = integer("season").index()
    val points = integer("points").default(0)
    val highestScore = integer("highestScore").default(0)
    val victories = integer("victories").default(0)
    val defeats = integer("defeats").default(0)
}