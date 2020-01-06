package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.events.PlayerLoggedInEvent
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class ChangePassCommand(val m: DreamAuth) : SparklyCommand(arrayOf("changepass", "changepassword", "trocarpassword", "trocarsenha")) {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage(
				generateCommandInfo("changepass",
						mapOf("Senha1" to "Sua nova senha", "Senha2" to "Sua nova senha novamente"),
						listOf(
								"Não use senhas que você já usou em outros servidores!",
								"Coloque caracteres especiais (como \$#@!%&*) para deixar a sua senha mais complexa!",
								"Não coloque senhas simples como \"1234\" ou \"${player.name}\"!"
						)
				)
		)
	}

	@Subcommand
	fun changePassword(player: Player, password1: String, password2: String) {
		m.checkIfNotRegistered(player)
		if (!m.isPasswordSecure(player, "changepass", password1, password2))
			return

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			// Como criar uma senha com o BCrypt é pesado, vamos mudar para uma task async
			// gerar hashed password
			val salt = BCrypt.gensalt(DreamAuth.WORKLOAD)
			val hashed = BCrypt.hashpw(password1, salt)

			val authInfo = m.uniqueId2PlayerInfo[player.uniqueId]!!

			transaction(Databases.databaseNetwork) {
				authInfo.password = hashed
			}

			switchContext(SynchronizationContext.SYNC)
			val event = PlayerLoggedInEvent(player)
			Bukkit.getPluginManager().callEvent(event)
			if (event.isCancelled)
				return@schedule

			player.sendMessage("§aA senha da sua conta foi alterada com sucesso! ^-^")
			player.sendMessage("§aLembre-se, jamais compartilhe sua senha! Guarde ela com carinho em um lugar que você possa lembrar!")
		}
	}
}