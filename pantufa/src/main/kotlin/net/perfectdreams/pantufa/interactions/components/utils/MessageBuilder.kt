package net.perfectdreams.pantufa.interactions.components.utils

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.interactiveButton
import net.perfectdreams.discordinteraktions.common.components.selectMenu
import net.perfectdreams.discordinteraktions.common.utils.inlineField
import net.perfectdreams.pantufa.dao.Command
import net.perfectdreams.pantufa.dao.Transaction
import net.perfectdreams.pantufa.interactions.components.ChangePageButtonClickExecutor
import net.perfectdreams.pantufa.interactions.components.TransactionFilterSelectMenuExecutor
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

private val expiredPanelMessage = PantufaReply("Ops, parece que essa mensagem é velha de mais. Por favor, use o comando novamente")
val invalidPageMessage = PantufaReply("Como é que você vai ver uma página que não existe, bobinhx?")
private val notForYouMessage = PantufaReply("Essa mensagem não é para você, bobinhx.")

suspend fun ComponentContext.invalid() {
    sendEphemeralMessage {
        content = expiredPanelMessage.build(sender.id.value.toLong())
    }
}

suspend fun ComponentContext.notForYou() {
    sendEphemeralMessage {
        content = notForYouMessage.build(sender.id.value.toLong())
    }
}

private val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMAN)
private val decimalFormatter = DecimalFormat("##.##")
private val Double.formatted get() = "`${decimalFormatter.format(this)}`"
private typealias Message = InteractionOrFollowupMessageModifyBuilder

/**
 * Transactions message
 */
fun BaseMessagePanelData.buildTransactionsMessage(page: Long, selfId: UUID?) = Message().apply {
    val transactions = fetchPage<Transaction>(page)

    embed {
        title = "Transações \u2014 Página ${page + 1}"
        color = Color(0x2090DF)

        description = transactions.joinToString("\n") {
            val timestamp = it.time / 1000

            "[<t:$timestamp:d> <t:$timestamp:t> | <t:$timestamp:R>] " +
            it.type.buildDisplayMessage.invoke(TransactionContext(it))
        }

        footer {
            text = if (size > 0) "Transações encontradas: ${numberFormatter.format(size)}"
                else "Nenhuma transação foi encontrada com esses critérios"
        }
    }

    val identifier = MessageIdentifier(key, page)

    components = mutableListOf(
        buildTransactionFilterSelectMenu(identifier),
        buildChangePageButtons(identifier, lastPage)
    )
}

/**
 * Commands log message
 */
fun BaseMessagePanelData.buildCommandsLogMessage(page: Long) = Message().apply {
    val commands = fetchPage<Command>(page).chunked(2)

    embed {
        title = "Log de comandos \u2014 Página ${page + 1}"
        color = Color(0x2090DF)

        // args.forEach { inlineField(EmbedBuilder.ZERO_WIDTH_SPACE, it) }

        commands.forEach { pair ->
            field {
                name = EmbedBuilder.ZERO_WIDTH_SPACE
                value = pair.joinToString("\n\n") {
                    val coords = "[`${it.world}` \u00BB ${it.x.formatted}, ${it.y.formatted}, ${it.z.formatted}]"
                    val timestamp = it.time / 1000

                    val message = buildString {
                        append("/${it.alias}")
                        it.args?.let { args -> append(" $args") }
                    }

                    ":calendar: [<t:$timestamp:d> <t:$timestamp:t> | <t:$timestamp:R>]\n:map: $coords **${it.player}**: `$message`"
                }
            }
        }

        footer {
            text = if (size > 0) "Comandos encontrados: ${numberFormatter.format(size)}"
                else "Nenhum comando foi encontrado com esses critérios"
        }
    }

    components = mutableListOf(buildChangePageButtons(MessageIdentifier(key, page), lastPage))
}

/**
 * Transaction filter select menu
 */
fun buildTransactionFilterSelectMenu(identifier: MessageIdentifier) = ActionRowBuilder().apply {
    selectMenu(
        TransactionFilterSelectMenuExecutor,
        identifier.encoded
    ) {
        val panel = activeMessagePanels[identifier.key]!!
        allowedValues = 1 .. TransactionType.values().size

        TransactionType.values().forEach {
            option(it.displayName, it.name) {
                description = "Transações ${if (it == TransactionType.UNSPECIFIED) "" else "relacionadas a "}${it.description}"
                default = panel.showOnly?.contains(it) ?: true
                //emoji = it.emoji
            }
        }
    }
}

/**
 * Change page buttons
 */
fun buildChangePageButtons(identifier: MessageIdentifier, lastPage: Long) = ActionRowBuilder().apply {
    interactiveButton(
        ButtonStyle.Primary,
        ChangePageButtonClickExecutor,
        with (identifier) { copy(page = page - 1) }.encoded
    ) {
        emoji = Constants.LEFT_EMOJI
        disabled = identifier.page == 0L
    }

    interactiveButton(
        ButtonStyle.Primary,
        ChangePageButtonClickExecutor,
        with (identifier) { copy(page = page + 1) }.encoded
    ) {
        emoji = Constants.RIGHT_EMOJI
        disabled = identifier.page == lastPage
    }
}