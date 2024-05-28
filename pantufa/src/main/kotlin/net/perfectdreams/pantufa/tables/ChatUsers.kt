package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

object ChatUsers : IdTable<UUID>() {
	override val tableName: String
		get() = "survival_chatusers"

	val _id = uuid("id")

	// Parece idiota, mas precisa ser assim para poder fazer DSL insert corretamente
	override val id = _id.entityId()

	val nickname = text("nickname").nullable()
	val tag = text("tag").nullable()
	val playOneMinute = integer("play_one_minute").nullable().index()

	override val primaryKey = PrimaryKey(_id)
}