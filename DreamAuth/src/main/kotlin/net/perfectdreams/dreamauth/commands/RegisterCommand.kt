package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.tables.AuthStorage
import net.perfectdreams.dreamauth.utils.ilike
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class RegisterCommand(val m: DreamAuth) : SparklyCommand(arrayOf("register", "registrar")) {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage(
				generateCommandInfo("register",
						mapOf("Senha1" to "Sua senha", "Senha2" to "Sua senha novamente"),
						listOf(
								"Não use senhas que você já usou em outros servidores!",
								"Coloque caracteres especiais (como \$#@!%&*) para deixar a sua senha mais complexa!",
								"Não coloque senhas simples como \"1234\" ou \"${player.name}\"!"
						)
				)
		)
	}

	@Subcommand
	fun register(player: Player, password1: String) {
		m.checkIfRegistered(player)
		player.sendMessage("§cVocê precisa confirmar a sua senha! Confirme ela utilizando §6/registrar $password1 $password1")
	}

	@Subcommand
	fun handleRegister(player: Player, password1: String, password2: String) {
		m.checkIfRegistered(player)
		if (!m.isPasswordSecure(player, "register", password1, password2))
			return

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val ipCount = transaction(Databases.databaseNetwork) {
				AuthInfo.find {
					AuthStorage.lastIp eq player.address.address.hostAddress
				}.count()
			}

			if (ipCount > 3 && !player.address.address.hostAddress.startsWith("10.0.0")) { // PSPE
				player.sendMessage("§cVocê já tem várias contas registradas no mesmo IP!")
				return@schedule
			}

			val matchedUsers = transaction(Databases.databaseNetwork) {
				val users = User.find {
					Users.username ilike player.name
				}

				AuthInfo.find {
					AuthStorage.uniqueId inList users.map { it.id.value }
				}.toMutableList()
			}

			if (matchedUsers.isNotEmpty()) {
				player.sendMessage("§cVocê já registrou uma conta com esse nome!")
				return@schedule
			}

			// Como criar uma senha com o BCrypt é pesado, vamos mudar para uma task async
			// gerar hashed password
			val salt = BCrypt.gensalt(DreamAuth.WORKLOAD)
			val hashed = BCrypt.hashpw(password1, salt)

			val authInfo = transaction(Databases.databaseNetwork) {
				AuthInfo.new(player.uniqueId) {
					password = hashed
					lastIp = player.address.address.hostAddress
					lastLogin = System.currentTimeMillis()
					remember = false
					twoFactorAuthEnabled = false
				}
			}

			m.uniqueId2PlayerInfo[player.uniqueId] = authInfo

			switchContext(SynchronizationContext.SYNC)
			m.finishLogin(player)
		}
	}
}
