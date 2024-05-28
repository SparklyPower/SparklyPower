package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.interactions.components.utils.*
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.extensions.uuid
import org.jetbrains.exposed.sql.transactions.transaction

class TransactionsExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    inner class Options : ApplicationCommandOptions() {
        val currency = optionalString("currency", "Nome da moeda") {
            choice("MONEY", "Sonecas")
            choice("CASH", "Pesadelos")
        }
        val user = optionalString("user", "Nome do usuário")
        val page = optionalInteger("page", "A página que você quer visualizar")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val selfId = pantufa.retrieveDiscordAccountFromUser(context.sender.id.value.toLong())?.minecraftId

        val user = args[options.user]?.uuid()
        val currency = args[options.currency]?.let(TransactionCurrency::valueOf)

        /**
         * If the user has a connected Minecraft Account and does not specify either payer or receiver, we will
         * just fetch any transactions that they are a part of
         */
        val fetchedTransactions =
            if (user != null)
                Transaction.fetchTransactionsFromSingleUser(user, currency)
            else if (selfId != null)
                Transaction.fetchTransactionsFromSingleUser(selfId, currency)
            else {
                context.sendEphemeralMessage {
                    content = "Você não colocou um usuário!"
                }
                return
            }

        val size = transaction(Databases.sparklyPower) { fetchedTransactions.count() }

        val page = args[options.page]?.let {
            if (it < 1 || it * MessagePanelType.TRANSACTIONS.entriesPerPage > size) return context.reply(invalidPageMessage)
            it - 1
        } ?: 0

        val messageData = saveAndCreateData(
            size,
            context.sender.id,
            user ?: selfId!!,
            MessagePanelType.TRANSACTIONS,
            fetchedTransactions
        )

        context.interactionContext.sendMessage {
            messageData.buildTransactionsMessage(page, selfId).let {
                embeds = it.embeds
                components = it.components
            }
        }
    }
}
