package net.perfectdreams.pantufa.commands.server.administration

import com.github.salomonbrys.kotson.*
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.Server

class ExecuteCommand : AbstractCommand("execute", listOf("executar"), requiresMinecraftAccount = true) {
	override fun run(context: CommandContext) {
		val serverName = context.args.getOrNull(0)

		if (context.user.id != "123170274651668480")
			return

		val server = Server.getByInternalName(serverName ?: "???")
		if (serverName == null || server == null) {
			context.reply(
					PantufaReply(
							Server.servers.joinToString(", ", transform = { it.internalName })
					)
			)
			return
		}

		val args = context.args.toMutableList()
		args.removeAt(0)

		val payload = server.send(
				jsonObject(
						"type" to "executeCommand",
						"pipeOutput" to true,
						"command" to args.joinToString(" ")
				)
		)

		println(payload)

		val messages = payload["messages"].array
		val replies = messages.map { PantufaReply(it.string) }

		context.reply(
				*replies.toTypedArray()
		)
	}
}