package net.perfectdreams.dreamcore.utils

import org.bukkit.Location

class DreamConfig(val serverName: String, val bungeeName: String) {
	var withoutPermission = "§cVocê não tem permissão para fazer isto!"
	var blacklistedWorldsTeleport: List<String> = mutableListOf()
	var blacklistedRegionsTeleport: List<String> = mutableListOf()
	var isStaffPermission = "perfectdreams.staff"
	var databaseName = "perfectdreams"
	var serverDatabaseName = "perfectdreams_survival"
	var tablePrefix = "survival"
	var mongoDbIp = "10.0.0.3"
	var postgreSqlIp = "10.0.0.6"
	var postgreSqlPort = 5432
	var enablePostgreSql = true
	lateinit var postgreSqlUser: String
	lateinit var postgreSqlPassword: String
	lateinit var spawn: Location
	lateinit var pantufaWebhook: String
	lateinit var pantufaInfoWebhook: String
	lateinit var pantufaErrorWebhook: String
	var socketPort = -1
	var defaultEventChannelId: String? = null
}