package net.perfectdreams.dreamcore

import com.charleskorn.kaml.Yaml
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import net.perfectdreams.dreamcore.commands.SkinCommand
import net.perfectdreams.dreamcore.commands.declarations.DreamCoreCommand
import net.perfectdreams.dreamcore.commands.declarations.MeninaCommand
import net.perfectdreams.dreamcore.commands.declarations.MeninoCommand
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.eventmanager.DreamEventManager
import net.perfectdreams.dreamcore.listeners.EntityListener
import net.perfectdreams.dreamcore.listeners.SkinsListener
import net.perfectdreams.dreamcore.listeners.SocketListener
import net.perfectdreams.dreamcore.network.socket.SocketServer
import net.perfectdreams.dreamcore.tables.*
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamcore.utils.npc.SparklyNPCManager
import net.perfectdreams.dreamcore.utils.scoreboards.SparklyScoreboardListener
import net.perfectdreams.dreamcore.utils.scoreboards.SparklyScoreboardManager
import net.perfectdreams.dreamcore.utils.skins.SkinUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
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
	val sparklyNPCManager = SparklyNPCManager(this)
	val scoreboardManager = SparklyScoreboardManager(this)
	val skinUtils = SkinUtils(this)
	val rpc = RPCUtils(this)
	val sparkSnap = SparkSnap(this)

	override fun onEnable() {
		saveDefaultConfig()

		loadConfig()

		dreamConfig.socket.let {
			logger.info { "Starting socket server at port ${it.port}" }
			thread { SocketServer(it.port).start() }
			Bukkit.getPluginManager().registerEvents(SocketListener(), this)
		}

		logger.info { "Starting Database..." }

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Users,
				EventVictories,
				Transactions,
				PreferencesTable,
				PlayerSkins
			)
		}

		logger.info { "Loading locales..." }
		TranslationUtils.loadLocale(dataFolder, "en_us")
		TranslationUtils.loadLocale(dataFolder, "pt_br")

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

		sparklyCommandManager.register(DreamCoreCommand(this))
		sparklyCommandManager.register(MeninoCommand(this))
		sparklyCommandManager.register(MeninaCommand(this))
		// Test command, should not be registered!
		// sparklyCommandManager.register(TestCommand, HelloWorldCommandExecutor(), HelloLoriCommandExecutor(), HelloCommandExecutor(), DoYouLikeCommandExecutor(), TellExecutor())

		// SparklySkinRestorer
		Bukkit.getPluginManager().registerEvents(SkinsListener(this), this)
		sparklyCommandManager.register(SkinCommand(this))

		// Scoreboards
		scoreboardManager.startScoreboardCleanUpTask()
		Bukkit.getPluginManager().registerEvents(SparklyScoreboardListener(this), this)

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

		ArmorStandHologram.loadArmorStandsIdsMarkedForRemoval()
		dreamEventManager.startEventsTask()
		sparklyNPCManager.start()
		sparkSnap.startTask()
	}

	fun loadConfig() {
		if (!config.contains("serverName")) {
			logger.severe { "Você esqueceu de colocar o \"serverName\" na configuração! Desligando servidor... :(" }
			Bukkit.shutdown()
			return
		}

		// Carregar configuração
		dreamConfig = Yaml.default.decodeFromString(config.saveToString())

		spawn = userData.getLocation("spawnLocation") ?: Bukkit.getWorlds().first().spawnLocation

		logger.info { "Let's make the world a better place, one plugin at a time. :3" }
		logger.info { "Server Name: ${dreamConfig.serverName}" }
		logger.info { "Bungee Server Name: ${dreamConfig.bungeeName}" }
	}

	override fun onDisable() {
		playerInventories.keys.forEach { it.restoreInventory() }
	}
}