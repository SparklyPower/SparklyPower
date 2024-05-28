package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.PlayerSonecas
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MoneyExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    inner class Options : ApplicationCommandOptions() {
        val playerName = optionalString("player_name", "Nome do Player")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val playerName = args[options.playerName]

        if (playerName != null) {
            val playerData = pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
                context.reply(
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

            context.reply(
                PantufaReply(
                    content = "**`${playerData.username}`** possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        } else {
            val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()

            val playerSonecasBalance = transaction(Databases.sparklyPower) {
                val playerSonecasData = PlayerSonecas.selectAll().where {
                    PlayerSonecas.id eq accountInfo.uniqueId
                }.firstOrNull()

                return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
            }

            context.reply(
                PantufaReply(
                    content = "Você possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        }
    }
}