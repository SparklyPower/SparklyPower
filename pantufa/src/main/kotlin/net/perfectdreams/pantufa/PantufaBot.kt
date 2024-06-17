package net.perfectdreams.pantufa

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.listeners.DiscordListener
import net.perfectdreams.loritta.morenitta.interactions.listeners.InteractionsListener
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.*
import net.perfectdreams.pantufa.utils.config.PantufaConfig
import net.perfectdreams.pantufa.utils.parallax.ParallaxEmbed
import net.perfectdreams.pantufa.utils.socket.SocketHandler
import net.perfectdreams.pantufa.utils.socket.SocketServer
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.minutes

class PantufaBot(val config: PantufaConfig) {
	companion object {
		const val PREFIX = "-"
		lateinit var INSTANCE: PantufaBot

		val http = HttpClient {
			expectSuccess = false
		}

		private val logger = KotlinLogging.logger {}
	}

	val interactivityManager = InteractivityManager()
	val executors = Executors.newCachedThreadPool()
	val coroutineDispatcher = executors.asCoroutineDispatcher()
	val coroutineMessageExecutor = createThreadPool("Message Executor Thread")
	val coroutineMessageDispatcher = coroutineMessageExecutor.asCoroutineDispatcher()
	fun createThreadPool(name: String) = Executors.newCachedThreadPool { r ->
		Thread(r, name).apply {
			isDaemon = true
		}
	}

	lateinit var interactionsListener: InteractionsListener
	lateinit var jda: JDA
	var mainLandGuild: Guild? = null
	val playersOnlineGraph = CachedGraphManager(config.grafana.token, "${config.grafana.url}/render/d-solo/JeZauCDnk/sparklypower-network?orgId=1&var-sparklypower_server=sparklypower_survival&var-world=All&panelId=87&width=800&height=300&tz=America%2FSao_Paulo")
	val tasksScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	fun start() {
		INSTANCE = this
		logger.info { "Starting Pantufa..." }

		initPostgreSql()

		logger.info { "Starting JDA..." }
		jda = JDABuilder.create(EnumSet.allOf(GatewayIntent::class.java))
			.addEventListeners(InteractionsListener(this))
			.setRawEventsEnabled(true) // Required for InteractionListener
			.setStatus(OnlineStatus.ONLINE)
			.setToken(config.token)
			.build()
			.awaitReady()

		interactionsListener = InteractionsListener(this)
		mainLandGuild = jda.getGuildById(config.sparklyPower.guild.idLong)

		logger.info { "Starting API server..." }

		val apiServer = APIServer(this)
		apiServer.start()

		logger.info { "Starting Pantufa's SocketServer thread..." }
		thread {
			val socket = SocketServer(60799)
			socket.socketHandler = object: SocketHandler {
				override fun onSocketReceived(json: JsonObject, response: JsonObject) {
					logger.info { "RECEIVED FROM SOCKET: $json" }
					val type = json["type"].nullString ?: return

					when (type) {
						"sendMessage" -> {
							// TODO: This should be replaced with webhooks within the applications that use this method
							val textChannelId = json["textChannelId"].string
							val message = json["message"].string
							val base64Image = json["image"].nullString

							val bufferedImage: BufferedImage? = null
							if (base64Image != null) {
								// TODO: Parse image
							}

							val messageBuilder = MessageCreateBuilder()
								.setContent(message)

							val embed = json["embed"].nullObj

							if (embed != null) {
								val parallaxEmbed = gson.fromJson<ParallaxEmbed>(embed)

								val discordEmbed = parallaxEmbed.toDiscordEmbed(true) // tá safe

								messageBuilder.setEmbeds(discordEmbed)
							}

							val textChannel = jda.getTextChannelById(textChannelId)

							textChannel?.sendMessage(messageBuilder.build())?.complete()
							return
						}

						"sendEventStart" -> {
							// TODO: This should be replaced with webhooks
							val guild = mainLandGuild
							if (guild != null) {
								val roleId = json["roleId"].string
								val channelId = json["channelId"].string
								val eventName = json["eventName"].string

								val role = guild.getRoleById(roleId) ?: return
								val channel = guild.getTextChannelById(channelId) ?: return

								channel.sendMessage("${role.asMention} Evento $eventName irá iniciar em 60 segundos!").queue()
							}
						}
					}

				}
			}
			socket.start()
		}

		logger.info { "Adding Event Listener..." }
		jda.addEventListener(DiscordListener(this))

		if (config.isTasksEnabled) {
			logger.info { "Starting Pantufa Tasks..." }
			PantufaTasks(this).start()
			scheduleCoroutineAtFixedRate(
				ServerVotesNotifier::class.simpleName!!,
				1.minutes,
				action = ServerVotesNotifier(this)
			)
		}

		logger.info { "Done! :3" }
	}

	fun initPostgreSql() {
		transaction(Databases.sparklyPower) {
			SchemaUtils.createMissingTablesAndColumns(
				DiscordAccounts,
				Users,
				NotifyPlayersOnline
			)
		}

		logger.info { "Starting PostgreSQL Notification Listener..." }
		Thread(
			null,
			PostgreSQLNotificationListener(
				Databases.dataSourceLoritta,
				mapOf(
					"loritta_lori_bans" to {
						// Someone got banned, omg!
						logger.info { "Received Loritta Ban for $it!" }

						GlobalScope.launch {
							val discordAccount = retrieveDiscordAccountFromUser(it.toLong())

							if (discordAccount != null && discordAccount.isConnected) {
								val userInfo = pantufa.getMinecraftUserFromUniqueId(discordAccount.minecraftId)

								if (userInfo != null) {
									logger.info { "Banning ${discordAccount.minecraftId} because their Discord account  ${discordAccount.discordId} is banned" }
									Server.PERFECTDREAMS_BUNGEE.send(
										jsonObject(
											"type" to "executeCommand",
											"player" to "Pantufinha",
											"command" to "ban ${userInfo.username} Banido da Loritta | ID da Conta no Discord: ${discordAccount.discordId}"
										)
									)
								} else {
									logger.info { "Ignoring Loritta Ban notification because the user $it doesn't have an associated user info data... Minecraft ID: ${discordAccount.minecraftId}" }
								}
							} else {
								logger.info { "Ignoring Loritta Ban notification because the user $it didn't connect an account..." }
							}
						}
					}
				)
			),
			"Loritta PostgreSQL Notification Listener"
		).start()
	}

	fun launchMessageJob(event: Event, block: suspend CoroutineScope.() -> Unit) {
		val coroutineName = when (event) {
			is MessageReceivedEvent -> "Message ${event.message} by user ${event.author} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is SlashCommandInteractionEvent -> "Slash Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is UserContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is MessageContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is CommandAutoCompleteInteractionEvent -> "Autocomplete for Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			else -> throw IllegalArgumentException("You can't dispatch a $event in a launchMessageJob!")
		}

		logger.info { coroutineName }

		val start = System.currentTimeMillis()
		val job = GlobalScope.launch(
			coroutineMessageDispatcher + CoroutineName(coroutineName),
			block = block
		)

		job.invokeOnCompletion {
			val diff = System.currentTimeMillis() - start
			if (diff >= 60_000) {
				logger.warn { "Message Coroutine $job took too long to process! ${diff}ms" }
			}
		}
	}

	/**
	 * Schedules [action] to be executed on [tasksScope] every [period] with a [initialDelay]
	 */
	private fun scheduleCoroutineAtFixedRate(
		taskName: String,
		period: kotlin.time.Duration,
		initialDelay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
		action: RunnableCoroutine
	) {
		logger.info { "Scheduling ${action::class.simpleName} to be ran every $period with a $initialDelay initial delay" }
		scheduleCoroutineAtFixedRate(taskName, tasksScope, period, initialDelay, action)
	}

	suspend fun <T> transactionOnSparklyPowerDatabase(statement: Transaction.() -> T): T {
		return withContext(Dispatchers.IO) {
			transaction(Databases.sparklyPower) {
				statement.invoke(this)
			}
		}
	}

	suspend fun <T> transactionOnLuckPermsDatabase(statement: Transaction.() -> T): T {
		return withContext(Dispatchers.IO) {
			transaction(Databases.sparklyPowerLuckPerms) {
				statement.invoke(this)
			}
		}
	}

	fun retrieveDiscordAccountFromUser(user: User): DiscordAccount? {
		return getDiscordAccountFromId(user.idLong)
	}

	suspend fun retrieveDiscordAccountFromUser(id: Long): DiscordAccount? {
		return transactionOnSparklyPowerDatabase {
			DiscordAccount.find { DiscordAccounts.discordId eq id }.firstOrNull()
		}
	}

	fun getDiscordAccountFromUser(user: User): DiscordAccount? {
		return getDiscordAccountFromId(user.idLong)
	}

	fun getDiscordAccountFromId(id: Long): DiscordAccount? {
		return transaction(Databases.sparklyPower) {
			DiscordAccount.find { DiscordAccounts.discordId eq id }.firstOrNull()
		}
	}

	fun getDiscordAccountFromUniqueId(uniqueId: UUID): DiscordAccount? {
		return transaction(Databases.sparklyPower) {
			DiscordAccount.find { DiscordAccounts.minecraftId eq uniqueId }.firstOrNull()
		}
	}

	fun getMinecraftUserFromUniqueId(uniqueId: UUID) = transaction(Databases.sparklyPower) {
		net.perfectdreams.pantufa.dao.User.findById(uniqueId)
	}

	suspend fun retrieveMinecraftUserFromUsername(username: String) = transactionOnSparklyPowerDatabase {
		net.perfectdreams.pantufa.dao.User.find { Users.username eq username }.firstOrNull()
	}

	fun getMinecraftUserFromUsername(username: String) = transaction(Databases.sparklyPower) {
		net.perfectdreams.pantufa.dao.User.find { Users.username eq username }.firstOrNull()
	}

	/**
	 * Gets how much time [uniqueId] spent online in the last [dayOffset]
	 */
	fun getPlayerTimeOnlineInTheLastXDays(uniqueId: UUID, dayOffset: Long): PlayerTimeOnlineResult {
		var survivalTrackedOnlineHours = Duration.ZERO

		val timestamp = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
			.minusDays(dayOffset)
			.withHour(0)
			.withMinute(0)
			.withSecond(0)
			.withNano(0)

		transaction(Databases.sparklyPower) {
			(this.connection as JdbcConnectionImpl)
				.connection
				.prepareStatement("select extract(epoch FROM SUM(logged_out - logged_in)) from survival_trackedonlinehours where player = ? and logged_out >= ?")
				.apply {
					this.setObject(1, uniqueId)
					this.setObject(2, timestamp)
				}
				.executeQuery()
				.also {
					while (it.next()) {
						survivalTrackedOnlineHours = Duration.ofSeconds(it.getLong(1))
					}
				}
		}

		return PlayerTimeOnlineResult(
			survivalTrackedOnlineHours,
			timestamp
		)
	}

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(coroutineDispatcher, block = block)
		return job
	}

	data class PlayerTimeOnlineResult(
		val duration: Duration,
		val since: OffsetDateTime
	)
}

val pantufa get() = PantufaBot.INSTANCE
val jda get() = PantufaBot.INSTANCE.jda
val jsonParser = JsonParser()
val gson = Gson()