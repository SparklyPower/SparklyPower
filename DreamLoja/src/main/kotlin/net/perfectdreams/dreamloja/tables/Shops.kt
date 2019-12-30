package net.perfectdreams.dreamloja.tables

import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.dao.LongIdTable

object Shops : LongIdTable() {
	override val tableName: String
		get() = "${DreamCore.dreamConfig.tablePrefix}_shops"

	val owner = uuid("owner").index()
	val shopName = text("shop_name").index()
	val worldName = text("world_name")
	val x = double("x")
	val y = double("y")
	val z = double("z")
	val yaw = float("yaw")
	val pitch = float("pitch")
}