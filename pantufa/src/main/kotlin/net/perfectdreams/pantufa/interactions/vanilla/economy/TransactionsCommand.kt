package net.perfectdreams.pantufa.interactions.vanilla.economy

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.interactions.vanilla.economy.transactions.TransactionsUtils.buildTransactionsMessage
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.MessagePanelType
import net.perfectdreams.pantufa.api.economy.TransactionCurrency
import net.perfectdreams.pantufa.utils.extensions.username
import net.perfectdreams.pantufa.utils.extensions.uuid
import net.perfectdreams.pantufa.utils.saveAndCreateData
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.transactions.transaction

class TransactionsCommand : SlashCommandDeclarationWrapper {
    companion object {
        const val TRANSACTIONS_PER_PAGE = 10
    }

    override fun command() = slashCommand("transactions", "Confira as transações mais recentes com base nos critérios escolhidos", CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true

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
                choice("MONEY", "Sonecas (Padrão)")
                choice("CASH", "Pesadelos")
            }
            val user = optionalString("user", "Nome do jogador")
            val page = optionalInteger("page", "Página que você deseja visualizar")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val selfId = context.pantufa.retrieveDiscordAccountFromUser(context.user.idLong)?.minecraftId
            val user = args[options.user]?.uuid()
            val currency = args[options.currency]?.let(TransactionCurrency::valueOf)

            val fetchedTransactions = if (user != null) {
                Transaction.fetchTransactionsFromSingleUser(user, currency)
            } else if (selfId != null) {
                Transaction.fetchTransactionsFromSingleUser(selfId, currency)
            } else {
                context.reply(true) {
                    styled(
                        "E o usuário nn sei"
                    )
                }
                return
            }

            val size = transaction(Databases.sparklyPower) { fetchedTransactions.count() }
            val page = args[options.page] ?: 0

            if (page < 0 || page * TRANSACTIONS_PER_PAGE > size) {
                context.reply(true) {
                    styled(
                        "Essa página não existe! Pelo visto você ou o Jogador ainda não é tão rico pra ter tantas transações assim..."
                    )
                }
                return
            }

            context.deferChannelMessage(false)

            val messageData = saveAndCreateData(
                size,
                context.user.idLong,
                user ?: selfId!!,
                MessagePanelType.TRANSACTIONS,
                fetchedTransactions
            )

            context.reply(false, messageData.buildTransactionsMessage(
                context.pantufa,
                currency,
                context.user.idLong,
                user?.username ?: selfId!!.username,
                0,
                selfId,
                emptyList()
            ))
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            var currency = args.getOrNull(0)
            var user = args.getOrNull(1)
            val page = args.getOrNull(2)?.toIntOrNull()
            val parser = hashMapOf(
                "pesadelos" to "CASH",
                "p" to "CASH",
                "sonecas" to "MONEY",
                "s" to "MONEY"
            )

            if (currency == null) {
                currency = "MONEY"
                if (user == null) {
                    user = context.retrieveConnectedMinecraftAccountOrFail().username

                    return if (page == null) {
                        mapOf(
                            options.currency to currency,
                            options.user to user,
                            options.page to 0
                        )
                    } else {
                        mapOf(
                            options.currency to currency,
                            options.user to user,
                            options.page to page
                        )
                    }
                } else {
                    return if (page == null) {
                        mapOf(
                            options.currency to currency,
                            options.user to user,
                            options.page to 0
                        )
                    } else {
                        mapOf(
                            options.currency to currency,
                            options.user to user,
                            options.page to page
                        )
                    }
                }
            } else {
                val parsedCurrency = parser[currency.lowercase()]

                if (parsedCurrency == null) {
                    context.explain()
                    return null
                } else {
                    if (user == null) {
                        user = context.retrieveConnectedMinecraftAccountOrFail().username

                        return if (page == null) {
                            mapOf(
                                options.currency to parsedCurrency,
                                options.user to user,
                                options.page to 0
                            )
                        } else {
                            mapOf(
                                options.currency to parsedCurrency,
                                options.user to user,
                                options.page to page
                            )
                        }
                    } else {
                        return if (page == null) {
                            mapOf(
                                options.currency to parsedCurrency,
                                options.user to user,
                                options.page to 0
                            )
                        } else {
                            mapOf(
                                options.currency to parsedCurrency,
                                options.user to user,
                                options.page to page
                            )
                        }
                    }
                }
            }
        }
    }
}