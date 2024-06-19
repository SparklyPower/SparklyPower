package net.perfectdreams.pantufa.interactions.vanilla.economy

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.dao.CashInfo
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.transactions.transaction

class PesadelosCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("pesadelos", "Veja quantos pesadelos você tem!", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("pesadelos")
        }

        executor = PesadelosCommandExecutor()
    }

    inner class PesadelosCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val playerName = optionalString("player_name", "Nome do Player")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val playerName = args[options.playerName]

            if (playerName != null) {
                val playerData = context.pantufa.retrieveMinecraftUserFromUsername(playerName) ?: run {
                    context.reply(false) {
                        styled(
                            "Player desconhecido!",
                            Constants.ERROR
                        )
                    }
                    throw SilentCommandException()
                }

                val playerUniqueId = playerData.id.value

                val cash = transaction(Databases.sparklyPower) {
                    CashInfo.findById(playerUniqueId)
                }?.cash ?: 0

                context.reply(false) {
                    styled(
                        "**`${playerData.username}`** possui **${cash} Pesadelos**!",
                        "\uD83D\uDCB5"
                    )
                }
            } else {
                val accountInfo = context.retrieveConnectedMinecraftAccount()!!
                val playerUniqueId = accountInfo.uniqueId

                val cash = transaction(Databases.sparklyPower) {
                    CashInfo.findById(playerUniqueId)
                }?.cash ?: 0

                context.reply(false) {
                    styled(
                        "Você possui **${cash} Pesadelos**!",
                        "\uD83D\uDCB5"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val playerName = args.getOrNull(0)

            return mapOf(
                options.playerName to playerName
            )
        }
    }
}