package net.perfectdreams.dreamcorebungee

import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.perfectdreams.dreamcorebungee.commands.BungeeCommandManager
import net.perfectdreams.dreamcorebungee.commands.DreamCoreBungeeCommand
import net.perfectdreams.dreamcorebungee.listeners.PlayerListener
import net.perfectdreams.dreamcorebungee.listeners.SocketListener
import net.perfectdreams.dreamcorebungee.network.socket.SocketServer
import net.perfectdreams.dreamcorebungee.tables.Users
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.DreamConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.concurrent.thread

class DreamCoreBungee : Plugin() {
	companion object {
		lateinit var dreamConfig: DreamConfig
		lateinit var INSTANCE: DreamCoreBungee
	}

	override fun onEnable() {
		super.onEnable()

		INSTANCE = this

		dataFolder.mkdirs()

		val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))

		// Carregar configuração
		dreamConfig = DreamConfig(config.getString("server-name"), config.getString("bungee-name")).apply {
			this.withoutPermission = config.getString("without-permission", "§cVocê não tem permissão para fazer isto!")
			this.isStaffPermission = config.getString("staff-permission", "perfectdreams.staff")
			this.databaseName = config.getString("database-name", "perfectdreams")
			this.mongoDbIp = config.getString("mongodb-ip", "10.0.0.3")
			this.serverDatabaseName = config.getString("server-database-name", "dummy")
			this.postgreSqlIp = config.getString("postgresql-ip", "10.0.0.6")
			this.postgreSqlPort = config.getInt("postgresql-port", 5432)
			this.postgreSqlUser = config.getString("postgresql-user", "postgres")
			this.postgreSqlPassword = config.getString("postgresql-password", "")
			this.pantufaWebhook = config.getString("webhooks.warn")
			this.pantufaInfoWebhook = config.getString("webhooks.info")
			this.pantufaErrorWebhook = config.getString("webhooks.error")
			this.socketPort = config.getInt("socket-port", -1)
		}

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
					Users
			)
		}

		if (dreamConfig.socketPort != -1) {
			thread { SocketServer(dreamConfig.socketPort).start() }
			this.proxy.pluginManager.registerListener(this, SocketListener())
		}

		this.proxy.pluginManager.registerListener(this, PlayerListener(this))

		val commandManager = BungeeCommandManager(this)

		commandManager.registerCommand(DreamCoreBungeeCommand())
	}

	override fun onDisable() {
		super.onDisable()
	}
}