package net.perfectdreams.pantufa.commands.server.administration

import com.github.salomonbrys.kotson.*
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces

class TpsCommand : AbstractCommand("tps") {
	override fun run(context: CommandContext) {
		val serverName = context.args.getOrNull(0)

		val server = Server.getByInternalName(serverName ?: "???")
		if (serverName == null || server == null) {
			context.reply(
					PantufaReply(
							Server.servers.joinToString(", ", transform = { it.internalName })
					)
			)
			return
		}

		val payload = server.send(
				jsonObject(
						"type" to "getTps"
				)
		)

		println(payload)

		val tps = payload["tps"].array

		context.reply(
				PantufaReply(
						"Atualmente ${server.internalName} está com ${tps[0].double.formatToTwoDecimalPlaces()} TPS!"
				)
		)
	}
}