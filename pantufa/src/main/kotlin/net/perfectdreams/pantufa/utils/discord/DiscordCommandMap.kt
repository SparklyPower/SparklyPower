package net.perfectdreams.pantufa.utils.discord

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.*
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import java.util.concurrent.CancellationException

class DiscordCommandMap(val pantufa: PantufaBot) : CommandMap<Command<CommandContext>> {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val commands = mutableListOf<Command<CommandContext>>()
	/* private val userCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS)
			.maximumSize(100)
			.build<Long, Long>().asMap() */

	override fun register(command: Command<CommandContext>) {
		logger.info { "Registering $command with ${command.labels}" }
		commands.add(command)
	}

	override fun unregister(command: Command<CommandContext>) {
		logger.info { "Unregistering $command..." }
		commands.remove(command)
	}

	suspend fun dispatch(ev: MessageReceivedEvent): Boolean {
		val rawMessage = ev.message.contentRaw

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commands) {
			if (dispatch(command, rawArguments, ev))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, ev: MessageReceivedEvent): Boolean {
		val message = ev.message.contentDisplay
		val user = ev.author

		var prefix = "-"

		val labels = command.labels.toMutableList()

		// Comandos com espaços na label, yeah!
		var valid = false
		var validLabel: String? = null

		val checkArguments = rawArguments.toMutableList()
		val rawArgument0 = checkArguments.getOrNull(0)
		var removeArgumentCount = 0
		val byMention = (rawArgument0 == "<@${pantufa.jda.selfUser.idLong}>" || rawArgument0 == "<@!${pantufa.jda.selfUser.idLong}>")

		if (byMention) {
			removeArgumentCount++
			checkArguments.removeAt(0)
			prefix = ""
		}

		for (label in labels) {
			val subLabels = label.split(" ")

			removeArgumentCount = if (byMention) { 1 } else { 0 }
			var validLabelCount = 0

			for ((index, subLabel) in subLabels.withIndex()) {
				val rawArgumentAt = checkArguments.getOrNull(index) ?: break

				val subLabelPrefix = if (index == 0)
					prefix
				else
					""

				if (rawArgumentAt.equals(subLabelPrefix + subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
					validLabelCount++
					removeArgumentCount++
				}
			}

			if (validLabelCount == subLabels.size) {
				valid = true
				validLabel = subLabels.joinToString(" ")
				break
			}
		}

		if (valid && validLabel != null) {
			val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
			val start = System.currentTimeMillis()

			val args = message.replace("@${ev.guild?.selfMember?.effectiveName ?: ""}", "").split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()
			val rawArgs = ev.message.contentRaw.split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()
			val strippedArgs = ev.message.contentStripped.split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()

			val allowedRoles = setOf(
				332650495522897920L, // SparklyPower staff,
				351473717194522647L, // Loritta staff
				493150630173605928L, // SparklyPower staff in Loritta's server
				505144985591480333L // Beeps & boops
			)

			if (ev.channel.idLong !in Constants.ALLOWED_CHANNELS_IDS && ev.guild.idLong != 268353819409252352L && ev.member?.roles?.any { it.idLong in allowedRoles } == false) { // Ideias Aleatórias
				ev.channel.sendMessage("${Constants.ERROR} **|** ${ev.author.asMention} Você só pode usar meus lindos e incríveis comandos nos canais de comandos!").complete()
				return true
			}

			repeat(removeArgumentCount) {
				args.removeAt(0)
				rawArgs.removeAt(0)
				strippedArgs.removeAt(0)
			}

			val context = CommandContext(
					pantufa,
					command,
					rawArgs,
					ev.message
			)

			if (ev.message.isFromType(ChannelType.TEXT)) {
				logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
			} else {
				logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
			}

			try {
				command.executor.invoke(context)

				val end = System.currentTimeMillis()
				if (ev.message.isFromType(ChannelType.TEXT)) {
					logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				} else {
					logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				}
				return true
			} catch (e: Exception) {
				if (e is CancellationException) {
					logger.error(e) { "RestAction in command ${command::class.simpleName} has been cancelled" }
					return true
				}

				if (e is ErrorResponseException) {
					if (e.errorCode == 40005) { // Request entity too large
						if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.guildChannel.canTalk()))
							context.reply(
									PantufaReply(
											"A imagem é grande demais!",
											"\uD83E\uDD37"
									)
							)
						return true
					}
				}

				if (e is SilentCommandException)
					return true

				if (e is CommandException) {
					context.reply(
							PantufaReply(
									e.reason,
									e.prefix
							)
					)
					return true
				}

				logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)

				// Avisar ao usuário que algo deu muito errado
				val mention = "${ev.author.asMention} "
				var reply = "\uD83E\uDD37 **|** " + mention + "Alguma coisa deu errado ao tentar executar o comando"

				if (!e.message.isNullOrEmpty())
					reply += " `${e.message!!}`"

				if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.guildChannel.canTalk()))
					ev.channel.sendMessage(reply).queue()

				return true
			}
		}
		return false
	}
}