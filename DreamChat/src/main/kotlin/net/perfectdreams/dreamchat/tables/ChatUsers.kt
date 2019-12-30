package net.perfectdreams.dreamchat.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.IdTable
import java.util.*

object ChatUsers : IdTable<UUID>() {
	override val tableName: String
		get() = "${DreamCore.dreamConfig.tablePrefix}_chatusers"

	val _id = uuid("id").primaryKey()

	// Parece idiota, mas precisa ser assim para poder fazer DSL insert corretamente
	override val id = _id.entityId()

	val nickname = text("nickname").nullable()
	val tag = text("tag").nullable()
}