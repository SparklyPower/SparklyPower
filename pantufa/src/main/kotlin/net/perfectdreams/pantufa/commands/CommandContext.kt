package net.perfectdreams.pantufa.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.utils.PantufaReply

class CommandContext(val event: MessageReceivedEvent, val discordAccount: DiscordAccount?, val minecraftAccountInfo: AbstractCommand.MinecraftAccountInfo?) {
	val member = event.member
	val user = event.author
	val args: List<String>

	init {
		val _args = event.message.contentRaw.split(" ").toMutableList()
		_args.removeAt(0)
		args = _args
	}

	fun reply(vararg pantufaReplies: PantufaReply): Message {
		val message = StringBuilder()
		for (pantufaReply in pantufaReplies) {
			message.append(pantufaReply.build(this) + "\n")
		}
		return sendMessage(message.toString())
	}

	fun sendMessage(content: String): Message {
		return event.channel.sendMessage(content).complete()
	}

	fun sendMessage(vararg replies: PantufaReply): Message {
		val content = replies.joinToString("\n", transform = { it.build(this) })
		return event.channel.sendMessage(content).complete()
	}

	fun sendMessage(content: MessageEmbed): Message {
		return event.channel.sendMessageEmbeds(content).complete()
	}
}