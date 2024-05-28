package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import java.awt.Color

class ChatColorExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    inner class Options : ApplicationCommandOptions() {
        val red = integer("red", "Quantidade de cor vermelha (em formato (R, G, B), é o primeiro número)")
        val green = integer("green", "Quantidade de cor verde (em formato (R, G, B), é o segundo número)")
        val blue = integer("blue", "Quantidade de cor azul (em formato (R, G, B), é o terceiro número)")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val r = args[options.red].toInt()
        val g = args[options.green].toInt()
        val b = args[options.blue].toInt()

        if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
            context.reply(
                PantufaReply(
                    "Cor inválida!",
                    Constants.ERROR
                )
            )
        }

        val color = Color(r, g, b)
        val hex = String.format("%02x%02x%02x", color.red, color.green, color.blue)

        val strBuilder = buildString {
            this.append("&x")
            hex.forEach {
                this.append("&")
                this.append(it)
            }
        }

        context.reply(
            PantufaReply(
                "Formato de cor para o chat: `$strBuilder`"
            )
        )
    }
}