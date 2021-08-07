package net.perfectdreams.dreamnetworkbans

import club.minnced.discord.webhook.WebhookClient
import net.perfectdreams.dreamcorebungee.KotlinPlugin
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamnetworkbans.commands.*
import net.perfectdreams.dreamnetworkbans.listeners.LoginListener
import net.perfectdreams.dreamnetworkbans.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.config.ConfigurationProvider
import net.perfectdreams.dreamcorebungee.utils.discord.DiscordWebhook
import net.perfectdreams.dreamnetworkbans.listeners.ServerConnectListener
import net.perfectdreams.dreamnetworkbans.listeners.SocketListener
import net.perfectdreams.dreamnetworkbans.utils.ASNManager
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class DreamNetworkBans : KotlinPlugin() {
	companion object {
		lateinit var INSTANCE: DreamNetworkBans
		var bypassPremiumCheck = false
	}

	val youtubersFile by lazy { File(this.dataFolder, "youtubers.json") }
	var youtuberNames = mutableSetOf<String>()

	val staffIps by lazy { File(this.dataFolder, "staff_ips.json") }
	val asnManager = ASNManager(this)

	val config by lazy {
		ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))
	}

	val adminChatWebhook by lazy {
		WebhookClient.withUrl(config.getString("adminchat-webhook"))
	}

	val punishmentWebhook by lazy {
		WebhookClient.withUrl(config.getString("punishment-webhook"))
	}

	val loggedInPlayers = Collections.newSetFromMap(ConcurrentHashMap<UUID, Boolean>())
	val minecraftMojangApi = MinecraftMojangAPI()

	override fun onEnable() {
		super.onEnable()
		INSTANCE = this

		// Caso seja reload
		loggedInPlayers.addAll(this.proxy.players.map { it.uniqueId })

		this.dataFolder.mkdirs()
		// Load ASN Manager data
		asnManager.load()

		registerCommand(BanCommand(this))
		registerCommand(CheckBanCommand(this))
		registerCommand(FingerprintCommand(this))
		registerCommand(DupeIpCommand(this))
		registerCommand(IPReportCommand(this))
		registerCommand(KickCommand(this))
		registerCommand(UnbanCommand(this))
		registerCommand(UnwarnCommand())
		registerCommand(WarnCommand(this))
		registerCommand(YouTuberAssistCommand(this))
		registerCommand(AdminChatCommand(this))
		registerCommand(DiscordCommand(this))
		registerCommand(GeoIpCommand(this))
		registerCommand(IpBanCommand(this))
		registerCommand(IpUnbanCommand(this))
		registerCommand(AdvDupeIpCommand(this))
		registerCommand(PremiumCommand(this))

		this.proxy.pluginManager.registerListener(this, LoginListener(this))
		this.proxy.pluginManager.registerListener(this, ServerConnectListener(this))
		this.proxy.pluginManager.registerListener(this, SocketListener(this))

		if (!youtubersFile.exists()) {
			youtubersFile.createNewFile()
			youtubersFile.writeText("[]")
		}

		if (!staffIps.exists()) {
			staffIps.createNewFile()
			staffIps.writeText("{}")
		}

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
					Bans,
					IpBans,
					Warns,
					Fingerprints,
					GeoLocalizations,
					ConnectionLogEntries,
					PremiumUsers
			)
		}
	}
}
