package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled
import java.awt.Color

class ChatColorCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("chatcolor", "Mude o estilo de um texto para o chat do Minecraft", CommandCategory.MINECRAFT) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("chatcolor")
        }

        executor = ChatColorCommandExecutor()
    }

    inner class ChatColorCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val red = integer("red", "Quantidade de cor vermelha (0-255)")
            val green = integer("green", "Quantidade de cor verde (0-255)")
            val blue = integer("blue", "Quantidade de cor azul (0-255)")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val red = args[options.red]
            val green = args[options.green]
            val blue = args[options.blue]

            if (red !in 0..255 || green !in 0..255 || blue !in 0..255) {
                context.reply(false) {
                    styled(
                        "Cor inválida!",
                        Constants.ERROR
                    )
                }
            }

            val color = Color(red, green, blue)
            val hex = String.format("%02x%02x%02x", color.red, color.green, color.blue)

            val strBuilder = buildString {
                append("&x")
                hex.forEach {
                    append("&")
                    append(it)
                }
            }

            context.reply(false) {
                styled(
                    "Formato de cor para o chat: `$strBuilder`"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val red = args.getOrNull(0)?.toInt()
            val green = args.getOrNull(1)?.toInt()
            val blue = args.getOrNull(2)?.toInt()

            if (red == null || green == null || blue == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.red to red,
                options.green to green,
                options.blue to blue
            )
        }
    }
}