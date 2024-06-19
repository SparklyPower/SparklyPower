package net.perfectdreams.pantufa.interactions.vanilla.economy

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.PlayerSonecas
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class SonecasCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("sonecas", "Veja quantas sonecas você e outros jogadores do SparklyPower possuem!", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("sonecas")
            add("bal")
            add("atm")
        }

        executor = SonecasCommandExecutor()
    }

    inner class SonecasCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

                val playerSonecasBalance = transaction(Databases.sparklyPower) {
                    val playerSonecasData = PlayerSonecas.selectAll().where {
                        PlayerSonecas.id eq playerUniqueId
                    }.firstOrNull()

                    return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
                }

                context.reply(false) {
                    styled(
                        "**`${playerData.username}`** possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
                        "\uD83D\uDCB5"
                    )
                }
            } else {
                val accountInfo = context.retrieveConnectedMinecraftAccount()!!

                val playerSonecasBalance = transaction(Databases.sparklyPower) {
                    val playerSonecasData = PlayerSonecas.selectAll().where {
                        PlayerSonecas.id eq accountInfo.uniqueId
                    }.firstOrNull()

                    return@transaction playerSonecasData?.get(PlayerSonecas.money)?.toDouble() ?: 0.0
                }

                context.reply(false) {
                    styled(
                        "Você possui **${playerSonecasBalance.formatToTwoDecimalPlaces()} Sonecas**!",
                        "\uD83D\uDCB5"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.playerName to args.getOrNull(0)
            )
        }
    }
}