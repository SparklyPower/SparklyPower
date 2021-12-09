package net.perfectdreams.dreamkits.tables

import com.google.gson.Gson
import net.perfectdreams.dreamcore.utils.exposed.jsonb
import net.perfectdreams.dreamkits.utils.PlayerKitsInfo
import org.jetbrains.exposed.sql.Table

object Kits : Table() {
	val id = uuid("id")
	val kitsInfo = jsonb("kits_info", PlayerKitsInfo::class.java, Gson())

	override val primaryKey = PrimaryKey(id)
}