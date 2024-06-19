package net.perfectdreams.pantufa.utils.extensions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
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