package net.perfectdreams.pantufa.utils.extensions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.perfectdreams.pantufa.utils.Emotes
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await() : T {
	return suspendCoroutine { cont ->
		this.queue({ cont.resume(it)}, { cont.resumeWithException(it) })
	}
}

fun MessageCreateAction.referenceIfPossible(message: Message): MessageCreateAction {
	if (message.isFromGuild && !message.guild.selfMember.hasPermission(message.channel as GuildChannel, Permission.MESSAGE_HISTORY))
		return this
	return this.setMessageReference(message)
}

suspend fun MessageHistory.retrievePastChunked(quantity: Int): List<Message> {
	val messages = mutableListOf<Message>()

	for (x in 0 until quantity step 100) {
		val newMessages = this.retrievePast(100).await()
		if (newMessages.isEmpty())
			break

		messages += newMessages
	}
	return messages
}

suspend fun MessageHistory.retrieveAllMessages(): List<Message> {
	val messages = mutableListOf<Message>()

	while (true) {
		val newMessages = this.retrievePast(100).await()
		if (newMessages.isEmpty())
			break

		messages += newMessages
	}

	return messages
}

/**
 * Converts an [Emote] to a JDA [Emoji]
 */
fun Emotes.Emote.toJDA() = when (this) {
	is Emotes.PantufaEmoji -> Emoji.fromCustom(
		this.name,
		this.id,
		this.animated
	)

	is Emotes.UnicodeEmote -> Emoji.fromUnicode(this.name)
}