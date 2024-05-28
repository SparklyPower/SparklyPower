package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction

class PesadelosExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
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

            val cash = transaction(Databases.sparklyPower) {
                CashInfo.findById(playerUniqueId)
            }?.cash ?: 0

            context.reply(
                PantufaReply(
                    content = "**`${playerData.username}`** possui **${cash} Pesadelos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        } else {
            val accountInfo = context.retrieveConnectedMinecraftAccountOrFail()
            val playerUniqueId = accountInfo.uniqueId

            val cash = transaction(Databases.sparklyPower) {
                CashInfo.findById(playerUniqueId)
            }?.cash ?: 0

            context.reply(
                PantufaReply(
                    content = "Você possui **${cash} Pesadelos**!",
                    prefix = "\uD83D\uDCB5"
                )
            )
        }
    }
}