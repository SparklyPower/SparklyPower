package net.perfectdreams.pantufa.api.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CommandContext(
		val pantufa: PantufaBot,
		val command: Command<CommandContext>,
		val args: List<String>,
		val message: Message
) {
	val sender = message.author
	val member = message.member

	suspend fun retrieveConnectedDiscordAccount() =
		pantufa.retrieveDiscordAccountFromUser(sender)

	suspend fun retrieveConnectedMinecraftAccount(): AbstractCommand.MinecraftAccountInfo? {
		val discordAccount = retrieveConnectedDiscordAccount() ?: return null

		val user = transaction(Databases.sparklyPower) {
			net.perfectdreams.pantufa.dao.User.find { Users.id eq discordAccount.minecraftId }.firstOrNull()
		}

		if (user == null) {
			message.channel.sendMessage("${Constants.ERROR} **|** ${sender.asMention} Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?").complete()
			return null
		}

		return AbstractCommand.MinecraftAccountInfo(
			discordAccount.minecraftId,
			user.username
		)
	}

	suspend fun retrieveConnectedMinecraftAccountOrFail(): AbstractCommand.MinecraftAccountInfo {
		return retrieveConnectedMinecraftAccount() ?: run {
			reply(
				PantufaReply(
					"Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
					Constants.ERROR
				)
			)

			throw SilentCommandException()
		}
	}

	suspend fun reply(vararg pantufaReplies: PantufaReply): Message {
		val message = StringBuilder()
		for (pantufaReply in pantufaReplies) {
			message.append(pantufaReply.build(this) + "\n")
		}
		return sendMessage(message.toString())
	}

	suspend fun sendMessage(content: String): Message {
		return message.channel.sendMessage(content).await()
	}

	suspend fun sendMessage(vararg replies: PantufaReply): Message {
		val content = replies.joinToString("\n", transform = { it.build(this) })
		return message.channel.sendMessage(content).await()
	}

	suspend fun sendMessage(content: MessageEmbed): Message {
		return message.channel.sendMessageEmbeds(content).await()
	}

	suspend fun user(argument: Int): User? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentions.users) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = pantufa.jda.retrieveUserById(link).await()

				if (user != null) // Pelo visto é!
					return user
			} catch (e: Exception) {
			}
		}
		return null
	}
}