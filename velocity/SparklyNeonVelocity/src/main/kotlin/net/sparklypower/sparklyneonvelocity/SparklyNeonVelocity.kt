package net.sparklypower.sparklyneonvelocity

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.util.Favicon
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.util.AddressUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.logger
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.event.HoverEvent
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.common.utils.toLegacySection
import net.sparklypower.sparklyneonvelocity.commands.*
import net.sparklypower.sparklyneonvelocity.config.SparklyNeonVelocityConfig
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.listeners.ChatListener
import net.sparklypower.sparklyneonvelocity.listeners.LoginListener
import net.sparklypower.sparklyneonvelocity.listeners.PingListener
import net.sparklypower.sparklyneonvelocity.listeners.ServerConnectListener
import net.sparklypower.sparklyneonvelocity.network.APIServer
import net.sparklypower.sparklyneonvelocity.tables.*
import net.sparklypower.sparklyneonvelocity.utils.ASNManager
import net.sparklypower.sparklyneonvelocity.utils.Pudding
import net.sparklypower.sparklyneonvelocity.utils.StaffColors
import net.sparklypower.sparklyneonvelocity.utils.emotes
import net.sparklypower.sparklyneonvelocity.utils.socket.SocketServer
import org.jetbrains.exposed.sql.SchemaUtils
import org.slf4j.Logger
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrNull

@Plugin(
    id = "sparklyneonvelocity",
    name = "SparklyNeonVelocity",
    version = "1.0.0-SNAPSHOT",
    url = "https://sparklypower.net",
    description = "I did it!", // Yay!!!
    authors = ["MrPowerGamerBR"]
)
class SparklyNeonVelocity @Inject constructor(private val server: ProxyServer, _logger: Logger, @DataDirectory dataDirectory: Path) {
    val logger = KotlinLogging.logger(_logger)

    val dataFolder = dataDirectory.toFile()
    val favicons = mutableMapOf<String, Favicon>()
    val isMaintenance = File(dataDirectory.toFile(), "maintenance").exists()

    val loggedInPlayers = Collections.newSetFromMap(ConcurrentHashMap<UUID, Boolean>())
    val pudding: Pudding
    val pingedByAddresses = Collections.newSetFromMap(
        Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build<String, Boolean>()
            .asMap()
    )
    val minecraftMojangApi = MinecraftMojangAPI()
    val asnManager = ASNManager(this)
    val punishmentManager = PunishmentManager(this, server)
    val apiServer = APIServer(this, server)
    val config: SparklyNeonVelocityConfig
    val punishmentWebhook: WebhookClient
    val adminChatWebhook: WebhookClient
    val discordAccountAssociationsWebhook: WebhookClient
    val lockedAdminChat = mutableSetOf<UUID>()

    init {
        logger.info { "Hello there! I made my first plugin with Velocity. SparklyNeonVelocity~" }
        config = Hocon.decodeFromConfig(ConfigFactory.parseFile(File(dataFolder, "plugin.conf")).resolve())
        punishmentWebhook = WebhookClient.withUrl(config.discord.webhooks.punishmentWebhook)
        adminChatWebhook = WebhookClient.withUrl(config.discord.webhooks.adminChatWebhook)
        discordAccountAssociationsWebhook = WebhookClient.withUrl(config.discord.webhooks.discordAccountAssociationsWebhook)

        pudding = Pudding.createPostgreSQLPudding(
            config.database.address,
            config.database.database,
            config.database.username,
            config.database.password,
            128
        )

        runBlocking {
            pudding.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    Bans,
                    IpBans,
                    Warns,
                    GeoLocalizations,
                    ConnectionLogEntries,
                    PremiumUsers,
                    BlockedASNs
                )
            }
        }

        loadFavicons()

        asnManager.load()

        apiServer.start()

        if (config.socketPort != null) {
            thread { SocketServer(this, server, config.socketPort).start() }
        }
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        // Do some operation demanding access to the Velocity API here.
        // For instance, we could register an event:
        // We don't need to register the main class because it is already registered!
        // The plugin main instance is automatically registered.
        server.eventManager.register(this, PingListener(this, this.server))
        server.eventManager.register(this, ServerConnectListener(this))
        server.eventManager.register(this, LoginListener(this, this.server))
        server.eventManager.register(this, ChatListener(this))

        server.commandManager.register("premium", PremiumCommand(this))
        server.commandManager.register("adminchat", AdminChatCommand(this, this.server), "a", "ademirchat")
        server.commandManager.register("advdupeip", AdvancedDupeIpCommand(this), "advanceddupeip", "advancedupeip")
        server.commandManager.register("ban", BanCommand(this, this.server))
        server.commandManager.register("checkban", CheckBanCommand(this, this.server))
        server.commandManager.register("connectionlog", ConnectionLogCommand(this))
        server.commandManager.register("discord", DiscordCommand(this))
        server.commandManager.register("dupeip", DupeIpCommand(this, this.server))
        server.commandManager.register("geoip", GeoIpCommand(this))
        server.commandManager.register("ipban", IpBanCommand(this, this.server), "banip", "banirip")
        server.commandManager.register("ipreport", IpReportCommand(this, this.server))
        server.commandManager.register("ipunban", IpUnbanCommand(this, this.server), "desbanirip", "unbanip", "ipdesbanir")
        server.commandManager.register("kick", KickCommand(this))
        server.commandManager.register("unban", UnbanCommand(this, this.server), "desbanir")
        server.commandManager.register("unwarn", UnwarnCommand(this, this.server))
        server.commandManager.register("warn", WarnCommand(this, this.server), "avisar")

        // Register our custom listeners
        // THIS REQUIRES SPARKLYVELOCITY!!!
        val proxyVersion = server.version
        if (proxyVersion.name == "SparklyVelocity") {
            val velocityServer = server as VelocityServer
            for (listener in config.listeners) {
                velocityServer.cm.bind(
                    listener.name,
                    AddressUtil.parseAndResolveAddress(listener.bind),
                    listener.proxyProtocol
                )
            }
        } else {
            logger.warn { "You aren't using SparklyVelocity! We aren't going to attempt to register another listeners then..." }
        }
    }

    private fun loadFavicons() {
        favicons.clear()
        File(dataFolder, "server-icons").listFiles().filter { it.extension == "png" } .forEach {
            this.logger.info { "Loading ${it.name}..." }
            val icon = ImageIO.read(it)
            favicons[it.nameWithoutExtension] = Favicon.create(icon)
        }
    }

    fun broadcastAdminChatMessage(sender: Player, text: String) {
        val staff = server.allPlayers
            .filter { it.hasPermission("sparklyneonvelocity.adminchat") && it.uniqueId in loggedInPlayers }

        val message = sender.let { player ->
            // The last color is a fallback, it checks for "group.default", so everyone should, hopefully, have that permission
            val role = StaffColors.values().first { player.hasPermission(it.permission) }

            val isGirl = runBlocking {
                pudding.transaction {
                    User.findById(player.uniqueId)?.isGirl ?: false
                }
            }

            val colors = role.colors
            val prefix = with (role.prefixes) { if (isGirl && size == 2) get(1) else get(0) }
            val emote = emotes[player.username] ?: ""

            // Using different colors for each staff group is bad, because it is harder to track admin chat messages since all groups have different colors
            var colorizedText = "$text"

            staff.forEach {
                val regex = Regex(".*\\b${it.username}\\b.*")
                if (!text.matches(regex)) return@forEach

                it.sendActionBar("${colors.chat.toLegacySection()}${player.username}${colors.nick.toLegacySection()} te mencionou no chat staff!".fromLegacySectionToTextComponent())
                it.playSound(
                    Sound.sound()
                        .type(Key.key("perfectdreams.sfx.msn"))
                        .volume(1f)
                        .pitch(1f)
                        .build()
                )

                colorizedText = colorizedText.replace(Regex("\\b${it.username}\\b"), colors.mention(it.username))
            }

            "$prefix $emote ${colors.nick.toLegacySection()}${player.username}${AdminChatCommand.adminChatColor.toLegacySection()}: $colorizedText".fromLegacySectionToTextComponent().apply {
                hoverEvent(HoverEvent.showText("§3Servidor: §b${player.currentServer.getOrNull()?.server?.serverInfo?.name}".fromLegacySectionToTextComponent()))
            }
        } ?: "\ue252 §x§a§8§a§8§a§8Mensagem do console: §x§c§6§b§f§c§3$text".fromLegacySectionToTextComponent()

        staff.forEach { it.sendMessage(message) }

        adminChatWebhook.send(
            WebhookMessageBuilder()
                .setUsername(sender.username)
                .setAvatarUrl("https://sparklypower.net/api/v1/render/avatar?name=${sender.username}&scale=16")
                .setContent(text)
                .build()
        )
    }
}