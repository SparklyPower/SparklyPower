package net.perfectdreams.pantufa.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

abstract class AbstractCommand(val label: String, val aliases: List<String> = listOf(), val requiresMinecraftAccount: Boolean = false) {
	fun matches(event: MessageReceivedEvent): Boolean {
		val message = event.message.contentDisplay

		val args = message.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		val labels = mutableListOf(label)
		labels.addAll(aliases)

		val valid = labels.any { command == PantufaBot.PREFIX + it }

		if (!valid)
			return false

		if (event.channel.idLong !in Constants.ALLOWED_CHANNELS_IDS && event.guild.idLong != 268353819409252352L && event.member?.roles?.any { it.idLong == 332650495522897920L } == false) { // Ideias Aleatórias
			event.channel.sendMessage("${Constants.ERROR} **|** ${event.author.asMention} Você só pode usar meus lindos e incríveis comandos nos canais de comandos!").complete()
			return true
		}

		event.channel.sendTyping().complete()

		val discordAccount = pantufa.getDiscordAccountFromId(event.author.idLong)

		var minecraftAccountInfo: MinecraftAccountInfo? = null

		if (requiresMinecraftAccount) {
			if (discordAccount == null || !discordAccount.isConnected) {
				event.channel.sendMessage("${Constants.ERROR} **|** ${event.author.asMention} Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!").complete()
				return true
			} else {
				val user = transaction(Databases.sparklyPower) {
					User.find { Users.id eq discordAccount.minecraftId }.firstOrNull()
				}

				if (user == null) {
					event.channel.sendMessage("${Constants.ERROR} **|** ${event.author.asMention} Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?").complete()
					return true
				}

				minecraftAccountInfo = MinecraftAccountInfo(
						discordAccount.minecraftId,
						user.username
				)
			}
		}

		run(
				CommandContext(
						event,
						discordAccount,
						minecraftAccountInfo
				)
		)
		return true
	}

	abstract fun run(context: CommandContext)

	data class MinecraftAccountInfo(
			val uniqueId: UUID,
			val username: String
	)
}