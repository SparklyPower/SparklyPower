package net.perfectdreams.pantufa.interactions.vanilla.economy

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.economy.TransactionCurrency
import net.perfectdreams.pantufa.utils.extensions.uuid
import net.perfectdreams.pantufa.interactions.vanilla.economy.transactions.TransactionsUtils

class TransactionsCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("transactions", "Confira as transações mais recentes com base nos critérios escolhidos", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true
        examples = listOf(
            "pesadelos MrPowerGamerBR |-| Verifica somente as transações de pesadelos do Jogador",
            "sonecas MrPowerGamerBR |-| Verifica somente as transações de sonecas do Jogador"
        )

        alternativeLegacyAbsoluteCommandPaths.apply {
            add("transactions")
            add("transações")
        }

        executor = TransactionsCommandExecutor()
    }

    inner class TransactionsCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val currency = optionalString("currency", "Moeda que você deseja ver as transações") {
                choice("Sonecas", "MONEY")
                choice("Pesadelos", "CASH")
            }
            val user = optionalString("user", "Nome do jogador")
            val page = optionalInteger("page", "Página que você deseja visualizar")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val selfId = context.retrieveConnectedMinecraftAccountOrFail().uniqueId
            val user = args[options.user]?.uuid() ?: selfId
            val currency = args[options.currency]?.let(TransactionCurrency::valueOf)
            val page = args[options.page] ?: 0

            context.deferChannelMessage(false)

            val messageData = TransactionsUtils.createMessage(
                context.pantufa,
                context.user.idLong,
                selfId,
                user,
                (if (page == 0) 0 else page - 1).toLong(),
                emptyList(),
                currency
            )

            context.reply(false, messageData)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val parser = hashMapOf(
                "pesadelos" to "CASH",
                "p" to "CASH",
                "sonecas" to "MONEY",
                "s" to "MONEY"
            )

            var currency: String?
            var user: String?
            var page: Int?
            val connectedAccount = context.retrieveConnectedMinecraftAccountOrFail()

            when (args.size) {
                // -transactions
                0 -> {
                    currency = null
                    user = null
                    page = 0
                }
                // -transactions [page || user || currency]
                1 -> {
                    if (args[0].toIntOrNull() == null) {
                        currency = parser[args[0]]
                        user = if (currency == null) {
                            args[0]
                        } else {
                            connectedAccount.username
                        }
                        page = 0
                    } else {
                        currency = null
                        user = null
                        page = args[0].toIntOrNull()
                    }
                }
                // -transactions [currency || user] [page || user]
                2 -> {
                    currency = parser[args[0]]
                    user = if (currency == null) {
                        args[0]
                    } else {
                        connectedAccount.username
                    }
                    page = if (args[1].toIntOrNull() == null) {
                        user = args[1]
                        0
                    } else {
                        args[1].toIntOrNull()
                    }
                }
                // -transactions [currency] [user] [page]
                3 -> {
                    currency = parser[args[0]]
                    user = args[1]
                    page = args[2].toIntOrNull()
                }

                else -> {
                    return null
                }
            }

            return mapOf(
                options.currency to currency,
                options.user to user,
                options.page to page
            )
        }
    }
}
