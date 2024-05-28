package net.perfectdreams.pantufa.utils

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.pantufa.commands.CommandContext

class PantufaReply(
    val content: String = " ",
    val prefix: String = Constants.LEFT_PADDING,
    val hasPadding: Boolean = true,
    val mentionUser: Boolean = true
) {
	fun build(commandContext: CommandContext) = build(commandContext.user)

	fun build(commandContext: net.perfectdreams.pantufa.api.commands.CommandContext) = build(commandContext.sender)

	fun build(user: User): String {
		var send = ""
		send = "$prefix **|** "
		if (mentionUser) {
			send = send + user.asMention + " "
		}
		send += content
		return send
	}

	fun build(userId: Long): String {
		var send = ""
		send = "$prefix **|** "
		if (mentionUser) {
			send = send + "<@${userId}>" + " "
		}
		send += content
		return send
	}
}