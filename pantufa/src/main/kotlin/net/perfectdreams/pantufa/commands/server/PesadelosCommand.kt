package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

object PesadelosCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "PesadelosCommand", listOf("pesadelos")) {
		executes {
			val playerName = args.getOrNull(0)

			if (playerName != null) {
				val playerData = pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
					reply(
						PantufaReply(
							content = "Player desconhecido!",
							prefix = Constants.ERROR
						)
					)
					throw SilentCommandException()
				}
				val playerUniqueId = playerData.id.value

				val cash = transaction(Databases.sparklyPower) {
					CashInfo.findById(playerUniqueId)
				}?.cash ?: 0

				reply(
					PantufaReply(
						content = "**`${playerData.username}`** possui **${cash} Pesadelos**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			} else {
				val accountInfo = retrieveConnectedMinecraftAccountOrFail()
				val playerUniqueId = accountInfo.uniqueId

				val cash = transaction(Databases.sparklyPower) {
					CashInfo.findById(playerUniqueId)
				}?.cash ?: 0

				reply(
					PantufaReply(
						content = "Você possui **${cash} Pesadelos**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			}
		}
	}
}