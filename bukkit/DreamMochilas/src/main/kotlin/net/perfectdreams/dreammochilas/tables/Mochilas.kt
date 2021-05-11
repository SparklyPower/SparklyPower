package net.perfectdreams.dreammochilas.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.LongIdTable

object Mochilas : LongIdTable() {
    override val tableName: String
        get() = DreamCore.dreamConfig.getTablePrefix() + "_mochilas"

    val owner = uuid("owner").index()
    val size = integer("size")
    val content = text("content")
    val funnyId = text("funny_id").nullable()
    val type = integer("type").nullable()
}