package net.perfectdreams.dreamcore

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.bukkit.BukkitCommandManager
import net.perfectdreams.dreamcore.commands.*
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.eventmanager.DreamEventManager
import net.perfectdreams.dreamcore.listeners.EntityListener
import net.perfectdreams.dreamcore.listeners.SocketListener
import net.perfectdreams.dreamcore.network.socket.SocketServer
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.DreamCommandManager
import net.perfectdreams.sparklypower.tables.LoggedInPlayers
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.concurrent.thread

class DreamCore : KotlinPlugin() {
	companion object {
		lateinit var dreamConfig: DreamConfig
		val INSTANCE
			get() = Bukkit.getPluginManager().getPlugin("DreamCore") as DreamCore
	}

	val dataYaml by lazy {
		File(dataFolder, "data.yml")
	}
	var spawn: Location? = null

	val userData by lazy {
		if (!dataYaml.exists())
			dataYaml.writeText("")

		YamlConfiguration.loadConfiguration(dataYaml)
	}

	val dreamEventManager = DreamEventManager()
	lateinit var dreamScriptManager: DreamScriptManager

	override fun onEnable() {
		saveDefaultConfig()

		loadConfig()

		dreamConfig.socket?.let {
			logger.info { "Starting socket server at port ${it.port}" }
			thread { SocketServer(it.port).start() }
			Bukkit.getPluginManager().registerEvents(SocketListener(), this)
		}

		val databaseConfig = dreamConfig.networkDatabase
		val databaseType = databaseConfig?.type ?: "SQLite"
		logger.info { "Starting Database... Database type: $databaseType" }

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Users,
				EventVictories
			)
		}

		logger.info { "Loading locales..." }
		TranslationUtils.loadLocale(dataFolder, "en_us")
		TranslationUtils.loadLocale(dataFolder, "pt_br")

		logger.info { "Preparing no flicker scoreboard in a separate thread..." }

		thread {
			PhoenixScoreboard.init()
		}

		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null)
			SignGUIUtils.registerSignGUIListener()

		Bukkit.getPluginManager().registerEvents(EntityListener(), this)
		Bukkit.getPluginManager().registerEvents(DreamMenuListener(), this)

		// Iniciar funções do Vault dentro de um try ... catch
		// É necessário ficar dentro de um try ... catch para caso o servidor não tenha algum
		// hook do Vault (por exemplo, não possuir um hook para o chat)
		try { VaultUtils.setupChat() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupEconomy() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupPermissions() } catch (e: NoClassDefFoundError) {}

		dreamCommandManager.registerCommand(DreamCoreCommand.command(this))
		dreamCommandManager.registerCommand(DreamCoreEvalCommand.command(this))
		dreamCommandManager.registerCommand(DreamCoreReloadCommand.command(this))
		dreamCommandManager.registerCommand(DreamCoreUnloadCommand.command(this))
		dreamCommandManager.registerCommand(DreamCoreSetSpawnCommand.command(this))
		dreamCommandManager.registerCommand(DreamCoreReloadConfigCommand.command(this))
		dreamCommandManager.registerCommand(MeninaCommand.command(this))
		dreamCommandManager.registerCommand(MeninoCommand.command(this))

		val scheduler = Bukkit.getScheduler()

		scheduler.schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				val newGirls = transaction(Databases.databaseNetwork) {
					User.find { Users.isGirl eq true }
						.map { it.id.value }
						.toMutableSet()
				}

				MeninaAPI.girls = newGirls
				waitFor(6000)
			}
		}

		dreamScriptManager = DreamScriptManager(this)
		dreamScriptManager.loadScripts()

		ArmorStandHologram.loadArmorStandsIdsMarkedForRemoval()
		dreamEventManager.startEventsTask()
	}

	fun loadConfig() {
		if (!config.contains("serverName")) {
			logger.severe { "Você esqueceu de colocar o \"serverName\" na configuração! Desligando servidor... :(" }
			Bukkit.shutdown()
			return
		}

		// Carregar configuração
		dreamConfig = DreamConfig(config)

		spawn = userData.getLocation("spawnLocation") ?: Bukkit.getWorlds().first().spawnLocation

		logger.info { "Let's make the world a better place, one plugin at a time. :3" }
		logger.info { "Server Name: ${dreamConfig.serverName}" }
		logger.info { "Bungee Server Name: ${dreamConfig.bungeeName}" }
	}

	override fun onDisable() {
		dreamScriptManager.unloadScripts()
	}
}