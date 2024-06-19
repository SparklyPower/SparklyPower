package net.perfectdreams.pantufa.api.commands

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.pantufa.utils.Constants

class PantufaReply(
	val content: String = " ",
	val prefix: String = Constants.LEFT_PADDING,
	val hasPadding: Boolean = true,
	val mentionUser: Boolean = true
) {
	fun build(context: UnleashedContext) = build(context.user)

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