package net.perfectdreams.dreamcorebungee.utils

class DreamConfig(val serverName: String, val bungeeName: String) {
	var withoutPermission = "§cVocê não tem permissão para fazer isto!"
	var isStaffPermission = "perfectdreams.staff"
	var databaseName = "perfectdreams"
	var serverDatabaseName = "perfectdreams_survival"
	var mongoDbIp = "10.0.0.3"
	var postgreSqlIp = "10.0.0.6"
	var postgreSqlPort = 5432
	lateinit var postgreSqlUser: String
	lateinit var postgreSqlPassword: String
	lateinit var pantufaWebhook: String
	lateinit var pantufaInfoWebhook: String
	lateinit var pantufaErrorWebhook: String
	var socketPort = -1
}