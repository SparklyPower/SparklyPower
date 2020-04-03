package net.perfectdreams.dreamassinaturas.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.IdTable
import java.util.*

object AssinaturaTemplates : IdTable<UUID>() {
    override val tableName: String
        get() = "${DreamCore.dreamConfig.getTablePrefix()}_assinaturatemplates"

    val _id = uuid("id").primaryKey()

    // Parece idiota, mas precisa ser assim para poder fazer DSL insert corretamente
    override val id = _id.entityId()

    val template = text("template")
}