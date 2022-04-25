package net.perfectdreams.dreamraffle.tables

import net.perfectdreams.dreamcore.DreamCore.Companion.dreamConfig
import org.jetbrains.exposed.dao.id.UUIDTable

object Gamblers : UUIDTable() {
    override val tableName get() = "${dreamConfig.getTablePrefix()}_dreamraffle_gamblers"
    val victories = integer("victories").default(0)
    val wonSonecas = long("won_sonecas").default(0)
    val spentSonecas = long("spent_sonecas").default(0)
    val wonCash = long("won_cash").default(0)
    val spentCash = long("spent_cash").default(0)
}