package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import java.awt.Color
import java.lang.StringBuilder

object ChatColorCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "ChatColorCommand", listOf("chatcolor")) {
		executes {
			val r = this.args.getOrNull(0)?.replace(",", "")?.trim()?.toInt() ?: return@executes
			val g = this.args.getOrNull(1)?.replace(",", "")?.trim()?.toInt() ?: return@executes
			val b = this.args.getOrNull(2)?.replace(",", "")?.trim()?.toInt() ?: return@executes

			if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
				reply(
						PantufaReply(
								"Cor inválida!",
								Constants.ERROR
						)
				)
			}

			val color = Color(r, g, b)
			val hex = String.format("%02x%02x%02x", color.red, color.green, color.blue)

			val strBuilder = buildString {
				this.append("&x")
				hex.forEach {
					this.append("&")
					this.append(it)
				}
			}

			reply(
					PantufaReply(
							"Formato de cor para o chat: `$strBuilder`"
					)
			)
		}
	}
}