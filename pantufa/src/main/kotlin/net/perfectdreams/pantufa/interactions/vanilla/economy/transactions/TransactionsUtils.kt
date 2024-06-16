package net.perfectdreams.pantufa.interactions.vanilla.economy.transactions

import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.economy.TransactionContext
import net.perfectdreams.pantufa.api.economy.TransactionCurrency
import net.perfectdreams.pantufa.api.economy.TransactionType
import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.utils.*
import net.perfectdreams.pantufa.utils.extensions.uuid
import java.text.NumberFormat
import java.util.*

object TransactionsUtils {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMAN)

    suspend fun BaseMessagePanelData.buildTransactionsMessage(
        pantufa: PantufaBot,
        currency: TransactionCurrency?,
        authorId: Long,
        query: String,
        page: Long,
        selfId: UUID?,
        userFacingTransactionType: List<SelectOption>
    ): MessageCreateData = MessageCreate {
        var currentPage = page
        val userFacingTransaction = userFacingTransactionType.toMutableList()
        val transactions: List<Transaction> = if (currency != null && currency == TransactionCurrency.CASH) {
            fetchWithCurrencyType(currentPage, TransactionCurrency.CASH)
        } else if (currency != null && currency == TransactionCurrency.MONEY) {
            fetchWithCurrencyType(currentPage, TransactionCurrency.MONEY)
        } else {
            fetchPage(currentPage)
        }

        embed {
            title = if (selfId == query.uuid()) {
                "Suas Transações — Página ${currentPage + 1}"
            } else {
                "Transações de $query — Página ${currentPage + 1}"
            }

            color = 0x2090DF

            description = transactions.joinToString("\n") {
                val timestamp = it.time / 1000
                "[<t:$timestamp:d> <t:$timestamp:t> | <t:$timestamp:R>] ${it.type.buildDisplayMessage.invoke(
                    TransactionContext(it)
                )}"
            }

            footer {
                name = if (size > 0) {
                    "Transações encontradas: ${numberFormatter.format(size)}"
                } else {
                    "Nenhuma transação foi encontrada com esses critérios"
                }
            }
        }

        actionRow(
            pantufa.interactivityManager
                .buttonForUser(
                    authorId,
                    ButtonStyle.PRIMARY,
                    builder = {
                        this.emoji = Emoji.fromCustom(
                            Constants.LEFT_EMOJI.name!!,
                            Constants.LEFT_EMOJI.id!!.value.toLong(),
                            Constants.LEFT_EMOJI.animated.discordBoolean
                        )

                        disabled = page == 0L
                    }
                ) {
                    currentPage--

                    val messageEditData = MessageEditBuilder.fromCreateData(
                        buildTransactionsMessage(pantufa, currency, authorId, query, currentPage, selfId, userFacingTransaction)
                    )

                    it.editMessage(false, messageEditData.build())
                },
            pantufa.interactivityManager
                .buttonForUser(
                    authorId,
                    ButtonStyle.PRIMARY,
                    builder = {
                        this.emoji = Emoji.fromCustom(
                            Constants.RIGHT_EMOJI.name!!,
                            Constants.RIGHT_EMOJI.id!!.value.toLong(),
                            Constants.RIGHT_EMOJI.animated.discordBoolean
                        )

                        disabled = page == lastPage
                    }
                ) {
                    currentPage++

                    val messageEditData = MessageEditBuilder.fromCreateData(
                        buildTransactionsMessage(pantufa, currency, authorId, query, currentPage, selfId, userFacingTransaction)
                    )

                    it.editMessage(false, messageEditData.build())
                }
        )
        actionRow(
            pantufa.interactivityManager
                .stringSelectMenuForUser(
                    authorId,
                    builder = {
                        maxValues = TransactionType.entries.size

                        if (showOnly == null) {
                            TransactionType.entries.forEach {
                                userFacingTransaction.add(
                                    SelectOption.of(it.displayName, it.name)
                                        .withDescription("Transações ${if (it == TransactionType.UNSPECIFIED) "" else "relacionadas a "}${it.description}")
                                        .withDefault(true)
                                )
                            }
                            showOnly = TransactionType.entries.toList()
                        }

                        addOptions(userFacingTransaction)
                    }
                ) { context, values ->
                    showOnly = values.map(TransactionType::valueOf)
                    val remainingTransactions = TransactionType.entries.toList().filter { it !in showOnly!! }
                    val refreshedOptions = mutableListOf<SelectOption>()

                    refreshedOptions.addAll(
                        showOnly!!.map {
                            SelectOption.of(it.displayName, it.name)
                                .withDescription("Transações ${if (it == TransactionType.UNSPECIFIED) "" else "relacionadas a "}${it.description}")
                                .withDefault(true)
                        }
                    )

                    refreshedOptions.addAll(
                        remainingTransactions.map {
                            SelectOption.of(it.displayName, it.name)
                                .withDescription("Transações ${if (it == TransactionType.UNSPECIFIED) "" else "relacionadas a "}${it.description}")
                        }
                    )

                    val messageEditData = MessageEditBuilder.fromCreateData(
                        buildTransactionsMessage(pantufa, currency, authorId, query, 0, selfId, userFacingTransaction)
                    )

                    context.editMessage(false, messageEditData.build())
                }
        )
    }
}