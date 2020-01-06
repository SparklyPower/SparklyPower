package net.perfectdreams.dreamauth

import com.github.salomonbrys.kotson.jsonObject
import com.google.common.cache.CacheBuilder
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.commands.*
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.events.PlayerLoggedInEvent
import net.perfectdreams.dreamauth.listeners.LoginListener
import net.perfectdreams.dreamauth.listeners.PlayerListener
import net.perfectdreams.dreamauth.listeners.SocketListener
import net.perfectdreams.dreamauth.tables.AuthStorage
import net.perfectdreams.dreamauth.utils.AuthConfig
import net.perfectdreams.dreamauth.utils.ConsoleFilter
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import org.apache.commons.codec.binary.Base32
import org.apache.commons.lang3.RandomStringUtils
import org.apache.logging.log4j.LogManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DreamAuth : KotlinPlugin() {
	companion object {
		const val WORKLOAD = 12
		const val QR_CODE_MAP_ID = 80
		val WHITELISTED_COMMANDS = listOf(
				"/login",
				"/logar",
				"/register",
				"/registrar",
				"/recovery",
				"/recuperar",
				"/2fa",
				"/twofactor",
				"/twofactorauth",
				"/twofactorauthentication"
		)
		val BASE_32 = Base32()
	}

	val uniqueId2PlayerInfo = ConcurrentHashMap<UUID, AuthInfo?>()
	val playerStatus = WeakHashMap<Player, PlayerStatus>()
	val wrongPasswordCount = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build<InetAddress, Int>()
			.asMap()
	val premiumUsers = Collections.synchronizedSet(
		mutableSetOf<UUID>()
	)

	var badPasswordsList = listOf<String>()
	lateinit var authConfig: AuthConfig
	// lateinit var service: MailgunService
	fun loadBadPasswordsList() {
		badPasswordsList = File(dataFolder, "bad_passwords.txt").readLines(Charsets.UTF_8).toMutableList()
	}

	override fun softEnable() {
		super.softEnable()
		transaction(Databases.databaseNetwork) {
			SchemaUtils.create(AuthStorage)
		}

		authConfig = AuthConfig().apply {
			this.mailgunBaseUrl = config.getString("mailgun.base-url")
			this.mailgunKey = config.getString("mailgun.key")
			if (config.contains("login-location")) {
				this.loginLocation = config.getSerializable("login-location", Location::class.java)
			}
		}

		loadBadPasswordsList()

		// service = MailgunServiceImpl(authConfig.mailgunKey,  authConfig.mailgunBaseUrl)

		val logger = (LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger)
		logger.addFilter(ConsoleFilter())

		registerEvents(LoginListener(this))
		registerEvents(PlayerListener(this))
		registerEvents(SocketListener(this))

		registerCommand(DreamAuthCommand(this))
		registerCommand(LoginCommand(this))
		registerCommand(RegisterCommand(this))
		registerCommand(ChangePassCommand(this))
		registerCommand(EmailCommand(this))
		registerCommand(RecuperarCommand(this))
		// registerCommand(TwoFactorAuthCommand(this))
		registerCommand(RememberCommand(this))

		registerCommand(UnregisterCommand())

		/* ProtocolLibrary.getProtocolManager().addPacketListener(object: PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.MAP) {
			override fun onPacketSending(p0: PacketEvent) {
				val map = WrapperPlayServerMap(p0.packet)
				if (map.itemDamage == DreamAuth.QR_CODE_MAP_ID)
					p0.isCancelled = true
			}
		}) */
	}

	fun finishLogin(player: Player) {
		DreamUtils.assertMainThread(true)
		val event = PlayerLoggedInEvent(player)
		Bukkit.getPluginManager().callEvent(event)
		if (event.isCancelled)
			return

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val playerInfo = uniqueId2PlayerInfo[player.uniqueId]!!

			transaction (Databases.databaseNetwork){
				playerInfo.lastIp = player.address.address.hostAddress
				playerInfo.lastLogin = System.currentTimeMillis()
			}

			DreamNetwork.PERFECTDREAMS_BUNGEE.send(
					jsonObject(
							"type" to "loggedIn",
							"player" to player.uniqueId.toString()
					)
			)

			switchContext(SynchronizationContext.SYNC)

			// Logado com sucesso!
			playerStatus[player] = PlayerStatus.LOGGED_IN
			wrongPasswordCount.remove(player.address.address)
		}
	}

	fun checkIfNotRegistered(player: Player): String {
		val authInfo = uniqueId2PlayerInfo[player.uniqueId] ?: throw ExecutedCommandException("§cVocê não está registrado! Registre a sua conta utilizando §6/register SuaSenha SuaSenha")

		return authInfo.password
	}

	fun checkIfRegistered(player: Player) {
		val authInfo = uniqueId2PlayerInfo[player.uniqueId]

		if (authInfo != null)
			throw ExecutedCommandException("§cVocê já está registrado! Entre na sua conta utilizando §6/login SuaSenha")
	}

	fun isPasswordSecure(player: Player, label: String, password1: String, password2: String): Boolean {
		if (6 > password1.length) {
			player.sendMessage("§cSua senha é pequena demais! Você precisa criar uma senha com no mínimo 6 caracteres!")
			return false
		}

		val randomPassword = RandomStringUtils.random(DreamUtils.random.nextInt(6, 13), 0, 66, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@!$&".toCharArray());

		for (_badPassword in badPasswordsList) {
			val badPassword = _badPassword.replace("%s", player.name)

			if (badPassword.equals(password1, true) || badPassword.equals(password1 + "1234", true) || badPassword.equals(password1 + "12345", true) || badPassword.equals(password1 + "123456", true) || badPassword.equals(password1 + "1234567", true) || badPassword.equals(password1 + "12345678", true) || badPassword.equals(password1 + "123456789", true) || badPassword.equals(password1 + "1234567890", true)) {
				player.sendMessage("§cSua senha é insegura! Para evitar pessoas entrando na sua conta, nós bloqueamos a sua senha. Por favor, escolha uma mais segura!")
				player.sendMessage("§7Precisa se uma senha segura? Aqui está uma para você: §f$randomPassword")
				player.sendMessage("§6/$label $randomPassword $randomPassword")
				player.sendMessage("§7Anote ela no seu celular, bloco de notas, na sua cabeça ou em qualquer lugar que você lembre aonde você guardou, mas NUNCA compartilhe ela para ninguém!")
				return false
			}

			for (i in 0..999) {
				if ((badPassword + i).equals(password1, true) || (i.toString() + badPassword).equals(password1, true) || (i.toString() + badPassword + i).equals(password1, true)) {
					player.sendMessage("§cSua senha é insegura! Para evitar pessoas entrando na sua conta, nós bloqueamos a sua senha. Por favor, escolha uma mais segura!")
					player.sendMessage("§7Precisa se uma senha segura? Aqui está uma para você: §f$randomPassword")
					player.sendMessage("§6/$label $randomPassword $randomPassword")
					player.sendMessage("§7Anote ela no seu celular, bloco de notas, na sua cabeça ou em qualquer lugar que você lembre aonde você guardou, mas NUNCA compartilhe ela para ninguém!")
					return false
				}
			}
		}

		if (password1 != password2) { // senhas diferentes
			player.sendMessage("§cAs duas senhas não coincidem! Verifique as duas senhas e tente novamente!")
			player.sendMessage("")
			player.sendMessage("§cSenha 1: §f$password1")
			player.sendMessage("§cSenha 2: §f$password1")
			player.sendMessage("")
			player.sendMessage("§6/$label $password1 $password1")
			return false
		}
		return true
	}

	override fun softDisable() {
		super.softDisable()
	}
}