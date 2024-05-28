package net.perfectdreams.pantufa.tables

object Profiles : SnowflakeTable() {
	val money = long("money").index()
}