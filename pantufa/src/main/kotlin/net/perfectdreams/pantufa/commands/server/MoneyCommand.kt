package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.PlayerSonecas
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object MoneyCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "MoneyCommand", listOf("money", "dinheiro", "bal", "balance")) {
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

				val playerSonecasBalance = transaction(Databases.sparklyPower) {
					val playerSonecasData = PlayerSonecas.selectAll().where {
						PlayerSonecas.id eq playerUniqueId
					}.firstOrNull()

					return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
				}

				reply(
					PantufaReply(
						content = "**`${playerData.username}`** possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			} else {
				val accountInfo = retrieveConnectedMinecraftAccountOrFail()

				val playerSonecasBalance = transaction(Databases.sparklyPower) {
					val playerSonecasData = PlayerSonecas.selectAll().where {
						PlayerSonecas.id eq accountInfo.uniqueId
					}.firstOrNull()

					return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
				}

				reply(
					PantufaReply(
						content = "Você possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
						prefix = "\uD83D\uDCB5"
					)
				)
			}
		}
	}
}