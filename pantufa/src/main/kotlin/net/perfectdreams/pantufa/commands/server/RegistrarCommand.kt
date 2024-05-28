package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class RegistrarCommand : AbstractCommand("registrar", listOf("register", "conectar", "connect")) {
	override fun run(context: CommandContext) {
		val arg0 = context.args.getOrNull(0)

		transaction(Databases.sparklyPower) {
			DiscordAccounts.deleteWhere { DiscordAccounts.discordId eq context.user.idLong }
		}

		if (arg0 != null) {
			val result = transaction(Databases.sparklyPower) {
				val user = User.find { Users.username eq arg0 }.firstOrNull()

				if (user == null) {
					context.reply(
							PantufaReply(
									"Usuário inexistente, você tem certeza que você colocou o nome certo?",
									Constants.ERROR
							)
					)
					return@transaction false
				}

				val connectedAccounts = DiscordAccount.find {
					DiscordAccounts.minecraftId eq user.id.value and (DiscordAccounts.isConnected eq true)
				}.count()

				if (connectedAccounts != 0L) {
					context.reply(
							PantufaReply(
									"A conta que você deseja conectar já tem uma conta conectada no Discord! Para desregistrar, utilize `/discord desregistrar` no servidor!",
									Constants.ERROR
							)
					)
					return@transaction false
				}

				DiscordAccount.new {
					this.minecraftId = user.id.value
					this.discordId = context.user.idLong
					this.isConnected = false
				}
				return@transaction true
			}

			if (!result)
				return

			context.reply(
					PantufaReply(
							"Falta pouco! Para terminar a integração, vá no SparklyPower e utilize `/discord registrar` para terminar o registro!",
							"<:lori_wow:626942886432473098>"
					)
			)
		} else {
			context.reply(
					PantufaReply(
							"`-registrar NomeNoServidor`"
					)
			)
		}
	}
}