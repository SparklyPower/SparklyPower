package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await

object MinecraftUserCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "MinecraftUserCommand", listOf("mcuser")) {
		executes {
			val user = user(0) ?: run {
				reply(
						PantufaReply(
								"Cadê o usuário nn sei"
						)
				)
				return@executes
			}

			val minecraftUser = pantufa.getDiscordAccountFromUser(user) ?: run {
				reply(
						PantufaReply(
								"O usuário ${user.asMention} não tem uma conta associada!"
						)
				)
				return@executes
			}

			val userInfo = pantufa.getMinecraftUserFromUniqueId(minecraftUser.minecraftId)

			reply(
					PantufaReply(
							"**Informações da conta de ${user.asMention}**"
					),
					PantufaReply(
							"**Nome:** `${userInfo?.username}`",
							mentionUser = false
					),
					PantufaReply(
							"**UUID:** `${minecraftUser.minecraftId}`",
							mentionUser = false
					),
					PantufaReply(
							"**A conta já foi conectada?** ${minecraftUser.isConnected}",
							mentionUser = false
					)
			)
		}
	}
}