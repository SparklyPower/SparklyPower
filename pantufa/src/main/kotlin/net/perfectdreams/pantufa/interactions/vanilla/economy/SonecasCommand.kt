package net.perfectdreams.pantufa.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.asDisabled
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.PlayerSonecas
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.api.minecraft.MinecraftAccountInfo
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Emotes
import net.sparklypower.rpc.TransferSonecasRequest
import net.sparklypower.rpc.TransferSonecasResponse
import net.sparklypower.rpc.UpdatePlayerSkinRequest
import net.sparklypower.rpc.UpdatePlayerSkinResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.NumberFormat
import java.util.*

class SonecasCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("sonecas", "TODOFIXTHISDATA", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true

        subcommand("atm", "Veja quantas sonecas você e outros jogadores do SparklyPower possuem!") {
            requireMinecraftAccount = true
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("sonecas")
                add("bal")
                add("atm")
                add("money")
            }

            executor = SonecasAtmCommandExecutor()
        }

        subcommand("pagar", "Transfira sonecas para outros usuários") {
            requireMinecraftAccount = true

            executor = SonecasPayCommandExecutor(PantufaBot.INSTANCE)
        }
    }

    inner class SonecasAtmCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

    class SonecasPayCommandExecutor(val m: PantufaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val playerName = string("player_name", "Nome do Player") {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    transaction(Databases.sparklyPower) {
                        Users.select(Users.username).where {
                            Users.username.like(focusedOptionValue.replace("%", "") + "%")
                        }
                            .limit(25)
                            .toList()
                    }.associate { it[Users.username] to it[Users.username] }
                }
            }

            val quantity = string("quantity", "Quantidade a ser transferida")
        }

        override val options = Options()

        val HANGLOOSE_EMOTES = listOf(
            Emotes.LoriHanglooseRight,
            Emotes.GabrielaHanglooseRight,
            Emotes.PantufaHanglooseRight,
            Emotes.PowerHanglooseRight
        )

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val playerName = args[options.playerName]
            val quantity = convertShortenedNumberToLong(args[options.quantity])

            if (quantity == null || quantity == 0.0 || formatSonecasAmount(quantity) == "0,00") {
                context.reply(false) {
                    styled(
                        "Uau, incrível! Você vai transferir *zero* sonecas, maravilha! Menos trabalho para mim, porque isso significa que não preciso preparar uma transação para você.",
                        Constants.ERROR
                    )
                }
                return
            }

            if (0.0 > quantity) {
                context.reply(false) {
                    styled(
                        "Uau, excelente! Você vai transferir sonecas *negativas*, extraordinário! Será que sonecas negativas seriam... *pesadelos*? Aí que medo não gosto dessas coisas sobrenaturais não... `/pesadelos`",
                        Constants.ERROR
                    )
                }
                return
            }

            if (quantity.isInfinite() || quantity.isNaN()) {
                context.reply(false) {
                    styled(
                        "Número inválido!",
                        Constants.ERROR
                    )
                }
                return
            }

            val minecraftAccount = context.retrieveConnectedMinecraftAccountOrFail()

            doTransfer(context, minecraftAccount, playerName, quantity, false)
        }

        suspend fun doTransfer(context: UnleashedContext, minecraftAccount: MinecraftAccountInfo, receiverName: String, quantity: Double, bypassLastActiveTime: Boolean) {
            val transferSonecasResponse = Json.decodeFromString<TransferSonecasResponse>(
                PantufaBot.http.post("${m.config.sparklyPower.server.sparklyPowerSurvival.apiUrl.removeSuffix("/")}/pantufa/transfer-sonecas") {
                    setBody(
                        Json.encodeToString(
                            TransferSonecasRequest(
                                minecraftAccount.username,
                                minecraftAccount.uniqueId.toString(),
                                receiverName,
                                quantity,
                                bypassLastActiveTime
                            )
                        )
                    )
                }.bodyAsText()
            )

            when (transferSonecasResponse) {
                TransferSonecasResponse.CannotTransferSonecasToSelf -> {
                    context.reply(false) {
                        styled(
                            "Transferência concluída com sucesso! Você recebeu *nada* de si mesmo, porque você está tentando transferir sonecas para si mesmo! Se você quer uma soneca de verdade, vá dormir.",
                            Constants.ERROR
                        )
                    }
                }
                is TransferSonecasResponse.NotEnoughSonecas -> {
                    context.reply(false) {
                        styled(
                            "Você não tem **${formatSonecasAmountWithCurrencyName(quantity)} para fazer isto! Você precisa conseguir mais ${formatSonecasAmountWithCurrencyName(quantity - transferSonecasResponse.currentUserMoney)} para continuar.",
                            Constants.ERROR
                        )
                    }
                }
                TransferSonecasResponse.PlayerHasNotJoinedRecently -> {
                    context.reply(false) {
                        styled(
                            "O player `${receiverName}` não entra a mais de 14 dias! Você tem certeza que você colocou o nome correto?",
                            Constants.ERROR
                        )

                        styled(
                            "Se você tem certeza que você colocou o nome correto, clique no botão para continuar!",
                            Emotes.PantufaLurk
                        )

                        actionRow(
                            m.interactivityManager.buttonForUser(
                                context.user,
                                ButtonStyle.PRIMARY,
                                "Continuar Transferência"
                            ) { context ->
                                context.editMessage(
                                    true,
                                    MessageEditBuilder.fromMessage(context.event.message)
                                        .apply {
                                            this.setComponents(context.event.message.actionRows.asDisabled())
                                        }
                                        .build()
                                )
                                doTransfer(context, minecraftAccount, receiverName, quantity, true)
                            }
                        )
                    }
                }
                TransferSonecasResponse.UserDoesNotExist -> {
                    context.reply(false) {
                        styled(
                            "Player não existe! Verifique se você colocou o nome do player corretamente.",
                            Constants.ERROR
                        )
                    }
                }
                is TransferSonecasResponse.Success -> {
                    // Let's add a random emoji just to look cute
                    val user1Emote = HANGLOOSE_EMOTES.random()
                    val user2Emote = HANGLOOSE_EMOTES.filter { it != user1Emote }.random()

                    context.reply(false) {
                        styled(
                            "Transferência realizada com sucesso! `${transferSonecasResponse.receiverName}` recebeu **${formatSonecasAmountWithCurrencyName(transferSonecasResponse.quantityGiven)}**!",
                            "\uD83E\uDD1D"
                        )

                        styled(
                            "`${minecraftAccount.username}` agora possui ${Emotes.Sonecas} **${formatSonecasAmountWithCurrencyName(transferSonecasResponse.selfMoney)}** e está em **#${transferSonecasResponse.selfRanking} lugar** no ranking!",
                            user1Emote
                        )

                        styled(
                            "`${transferSonecasResponse.receiverName}` agora possui ${Emotes.Sonecas} **${formatSonecasAmountWithCurrencyName(transferSonecasResponse.receiverMoney)}** e está em **#${transferSonecasResponse.receiverRanking} lugar** no ranking!",
                            user2Emote
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = TODO()

        // TODO: Move that out to a common module
        private val brazilLocale = Locale("pt", "BR")
        private val numberFormat = NumberFormat.getNumberInstance(brazilLocale)
            .apply {
                this.minimumFractionDigits = 2
                this.maximumFractionDigits = 2
            }

        fun formatSonecasAmount(amount: Double): String {
            return numberFormat.format(amount)
        }

        fun formatSonecasAmountWithCurrencyName(amount: Double): String {
            val formattedNumber = formatSonecasAmount(amount)

            return if (amount == 1.0) {
                "$formattedNumber soneca"
            } else {
                "$formattedNumber sonecas"
            }
        }

        /**
         * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Double] number
         *
         * This also converts a normal number (non shortened) to a [Double]
         *
         * @param input the shortened number
         * @return      the number as long or null if it is a non valid (example: text) number
         */
        fun convertShortenedNumberToLong(input: String): Double? {
            val inputAsLowerCase = input.lowercase()

            return when {
                inputAsLowerCase.endsWith("m") -> inputAsLowerCase.removeSuffix("m").toDoubleOrNull()?.times(1_000_000)
                inputAsLowerCase.endsWith("kk") -> inputAsLowerCase.removeSuffix("kk").toDoubleOrNull()?.times(1_000_000)
                inputAsLowerCase.endsWith("k") -> inputAsLowerCase.removeSuffix("k").toDoubleOrNull()?.times(1_000)
                else -> inputAsLowerCase.toDoubleOrNull()
            }
        }
    }
}