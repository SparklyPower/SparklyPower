package net.perfectdreams.dreamloja.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.LongIdTable

object UserShopVotes : LongIdTable() {
	override val tableName: String
		get() = "${DreamCore.dreamConfig.tablePrefix}_shopvotes"

	val givenBy = uuid("given_by")
	val receivedBy = uuid("received_by")
	val receivedAt = long("received_at")
}