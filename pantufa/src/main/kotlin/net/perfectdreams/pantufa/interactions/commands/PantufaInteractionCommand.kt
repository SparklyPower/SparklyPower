package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.utils.PantufaReply

abstract class PantufaInteractionCommand(
    val pantufa: PantufaBot
) : SlashCommandExecutor() {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        try {
            if (context !is GuildApplicationCommandContext || context.guildId !in pantufa.whitelistedGuildIds) {
                context.sendEphemeralMessage {
                    content = "Comandos apenas podem ser utilizados em nosso servidor oficial! https://discord.gg/sparklypower"
                }
                return
            }

            executePantufa(PantufaCommandContext(pantufa, context), args)
        } catch (e: SilentCommandException) {
            println("Caught *silent* cmd exception!")
        }
    }

    abstract suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments)

    /**
     * Appends a Loritta-styled formatted message to the builder's message content.
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
     *
     * If there's already content present in the builder, a new line will be inserted before the styled replied!
     *
     * @param content the already built LorittaReply
     */
    fun MessageBuilder.styled(reply: PantufaReply) {
        if (content != null) {
            content += "\n"
            content += "${reply.prefix} **|** ${reply.content}"
        } else {
            content = "${reply.prefix} **|** ${reply.content}"
        }
    }
}