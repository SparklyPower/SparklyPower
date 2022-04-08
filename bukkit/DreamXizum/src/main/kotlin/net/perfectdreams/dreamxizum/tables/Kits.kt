package net.perfectdreams.dreamxizum.tables

import net.perfectdreams.dreamcore.DreamCore.Companion.dreamConfig
import org.jetbrains.exposed.dao.id.IntIdTable

object Kits : IntIdTable() {
    override val tableName get() = "${dreamConfig.getTablePrefix()}_dreamxizum_kits"
    val uuid = uuid("uuid").index()
    val hash = integer("hash").index()
    val name = varchar("name", 12).nullable()
    val slot = integer("slot")
    val items = text("items")
    val armor = text("armor")
}