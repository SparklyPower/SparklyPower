package net.perfectdreams.dreamauth.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.events.PlayerLoggedInEvent
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.transactions.transaction

class LoginListener(val m: DreamAuth) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onAuthLogin(e: PlayerLoggedInEvent) {
		if (m.premiumUsers.contains(e.player.uniqueId)) {
			e.player.sendMessage("§aObrigado por suportar o desenvolvimento do Minecraft!")
		} else {
			e.player.sendMessage("§aSua conta foi logada com sucesso! Bom divertimento! ^-^")
			e.player.sendMessage("§aLembre-se, jamais compartilhe sua senha! Guarde ela com carinho em um lugar que você possa lembrar!")
			e.player.sendMessage("§3Tem Minecraft Original? Então utilize §6/premium§3 para deixar a sua conta mais segura e não precisar logar manualmente no servidor!")
			e.player.sendMessage("§3Não tem Minecraft Original? Então compre para ajudar o desenvolvimento do Minecraft!§b https://www.minecraft.net/get-minecraft")

			val authInfo = m.uniqueId2PlayerInfo[e.player.uniqueId] ?: return

			val hasEmail = authInfo.email != null
			val hasTwoFactorAuth = authInfo.twoFactorAuthEnabled

			if (false && !hasEmail) {
				e.player.sendMessage("§3Jamais perca acesso a sua conta! Use §6/email SeuEmail§3 para conseguir recuperar a sua conta caso você esqueça a sua senha!")
			}

			if (false && !hasTwoFactorAuth) {
				e.player.sendMessage("§3Jamais seja hackeado! Ative o modo de autenticação dupla usando o seu celular utilizando §6/2fa ativar§3 e fique seguro sabendo que nunca irão conseguir hackear a sua conta, mesmo se descobrirem a sua senha!")
			}
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		m.uniqueId2PlayerInfo.remove(e.player.uniqueId)
		m.playerStatus.remove(e.player)
	}

	@EventHandler
	fun onLogin(e: AsyncPlayerPreLoginEvent) {
		val authInfo = transaction(Databases.databaseNetwork) {
			AuthInfo.findById(e.uniqueId)
		}
		if (authInfo != null)
			m.uniqueId2PlayerInfo[e.uniqueId] = authInfo
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onJoin(e: PlayerJoinEvent) {
		e.player.teleport(m.authConfig.loginLocation ?: error("DreamAuth Login Location is not present!"))
		val playerAddress = e.player.address ?: error("Player address is null!")

		val authInfo = m.uniqueId2PlayerInfo[e.player.uniqueId]
		val player = e.player

		/* if (!m.uniqueId2PlayerInfo.contains(e.player.uniqueId)) {
			player.kickPlayer("§cAlguma coisa deu errado, sorry...\n\nTente reconectar ao servidor!")
			return
		} */

		val playerStatus = m.playerStatus[player]

		// Caso já tenha um destes status selecionados, vamos apenas ignorar, porque provavelmente o FastLogin
		// já processou o player
		if (playerStatus == PlayerStatus.LOGGED_IN || playerStatus == PlayerStatus.TWO_FACTOR_AUTH)
			return

		if (authInfo == null) { // Não registrado
			m.playerStatus[player] = PlayerStatus.REGISTER
			scheduler().schedule(m) {
				waitFor(20)
				player.sendTitle("§a§lBem-Vindo(a) ao...", "§4§lSparkly§b§lPower", 10, 60, 10)
				waitFor(90)
				player.sendTitle("§f", "§bMas, antes de começar...", 10, 60, 10)
				waitFor(90)
				player.sendTitle("§b§lRegistre a sua conta!", "§6/registrar SuaSenha SuaSenha", 10, 60, 10)
				waitFor(20)
				while (player.isOnline) {
					val playerStatus = m.playerStatus[player] ?: return@schedule
					if (playerStatus != PlayerStatus.REGISTER)
						return@schedule

					player.sendActionBar("§cUse uma senha segura para evitar idiotas tentando entrar na sua conta!")
					player.sendTitle("§b§lRegistre a sua conta!", "§6/registrar SuaSenha SuaSenha", 0, 40, 20)
					waitFor(20)
				}
			}
			return
		}

		m.logger.info("Premium Users: ${m.premiumUsers} Player UUID: ${e.player.uniqueId} Can Bypass? ${m.premiumUsers.contains(e.player.uniqueId)}")
		if (!m.premiumUsers.contains(e.player.uniqueId)) {
			val password = authInfo.password
			val lastIp = authInfo.lastIp
			val remember = authInfo.remember
			val userIp = playerAddress.address.hostAddress

			if (!remember) { // Registrado, o usuário não utiliza remember
				handleLogin(player)
				return
			}

			if (remember && lastIp != userIp) { // Registrado, o usuário utiliza /remember mas o IP não é o mesmo
				player.sendMessage("§cPor motivos de segurança é necessário que você entre novamente na sua conta")
				handleLogin(player)
				return
			}
		}

		m.playerStatus[player] = PlayerStatus.LOGGED_IN
		m.finishLogin(e.player)
	}

	fun handleLogin(player: Player) {
		m.playerStatus[player] = PlayerStatus.LOGIN
		scheduler().schedule(m) {
			waitFor(20)
			if (m.playerStatus[player] != PlayerStatus.LOGIN)
				return@schedule
			player.sendTitle("§a§lBem-Vindo(a) de volta!", "§3Sentimos saudades ;w;", 10, 60, 10)
			waitFor(90)
			if (m.playerStatus[player] != PlayerStatus.LOGIN)
				return@schedule
			player.sendTitle("§f", "§bMas, antes de começar...", 10, 60, 10)
			waitFor(90)
			if (m.playerStatus[player] != PlayerStatus.LOGIN)
				return@schedule
			player.sendTitle("§b§lEntre na sua conta!", "§6/login SuaSenha", 10, 60, 10)
			waitFor(20)
			if (m.playerStatus[player] != PlayerStatus.LOGIN)
				return@schedule

			for (i in 300  downTo 0) {
				val playerStatus = m.playerStatus[player] ?: return@schedule
				if (playerStatus != PlayerStatus.LOGIN)
					return@schedule

				player.sendActionBar("§cVocê tem $i segundos para entrar na sua conta!")
				player.sendTitle("§b§lEntre na sua conta!", "§6/login SuaSenha", 0, 40, 10)
				waitFor(20)
			}

			player.kickPlayer("§cVocê demorou demais para entrar...")
		}
	}
}
