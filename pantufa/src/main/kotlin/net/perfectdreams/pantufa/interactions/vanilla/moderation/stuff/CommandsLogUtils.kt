package net.perfectdreams.pantufa.interactions.vanilla.moderation.stuff

import dev.kord.rest.builder.message.EmbedBuilder
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.Command
import net.perfectdreams.pantufa.utils.BaseMessagePanelData
import net.perfectdreams.pantufa.utils.Constants
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object CommandsLogUtils {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.GERMAN)
    private val decimalFormatter = DecimalFormat("##.##")
    val Double.formatted get() = "`${decimalFormatter.format(this)}`"

    fun BaseMessagePanelData.buildCommandsLogMessage(
        pantufa: PantufaBot,
        authorId: Long,
        page: Long,
    ): MessageCreateData = MessageCreate {
        var currentPage = page
        val commands = fetchPage<Command>(page).chunked(2)

        embed {
            title = "Registro de Comandos — Página ${currentPage + 1}"
            color = 0x2090DF

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

                    inline = false
                }
            }

            footer {
                name = if (size > 0) {
                    "Comandos encontrados: ${numberFormatter.format(size)}"
                } else {
                    "Nenhum comando foi encontrado com esses critérios"
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
                        buildCommandsLogMessage(pantufa, authorId, currentPage)
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
                        buildCommandsLogMessage(pantufa, authorId, currentPage)
                    )

                    it.editMessage(false, messageEditData.build())
                }
        )
    }
}
