package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class EmailCommand(val m: DreamAuth) : SparklyCommand(arrayOf("email")) {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage(
				generateCommandInfo("email",
						mapOf("Email" to "Seu email"),
						listOf(
								"Após colocar o seu email, será possível recuperar a sua conta usando §6/recuperar email"
						)
				)
		)
	}

	@Subcommand
	fun handleEmail(player: Player, email: String) {
		m.checkIfNotRegistered(player)

		if (!email.contains("@")) {
			player.sendMessage("§9$email§c não é um email válido!")
			return
		}

		val authInfo = m.uniqueId2PlayerInfo[player.uniqueId]!!

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction(Databases.databaseNetwork) {
				authInfo.email = email
			}

			switchContext(SynchronizationContext.SYNC)

			player.sendMessage("§aVocê registrou o seu email com sucesso! Caso você esqueça a sua senha, você poderá recuperar a sua conta usando §6/recuperar $email§a!")
		}
	}
}