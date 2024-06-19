package net.perfectdreams.pantufa.interactions.vanilla.discord

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.commands.PantufaReply

class PingCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("ping", "Pong! \uD83C\uDFD3", CommandCategory.MISC) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("ping")
        }
        executor = PingExecutor()
    }

    inner class PingExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val time = System.currentTimeMillis()

            val replies = mutableListOf(
                PantufaReply(
                    content = "**Pong!**",
                    prefix = ":ping_pong:"
                ),
                PantufaReply(
                    content = "**Gateway Ping:** `${context.jda.gatewayPing}ms`",
                    prefix = ":stopwatch:",
                    mentionUser = false
                ),
                PantufaReply(
                    content = "**API Ping:** `...ms`",
                    prefix = ":stopwatch:",
                    mentionUser = false
                )
            )

            val message = context.reply(false) {
                content = replies.joinToString(separator = "\n", transform = { it.build(context) })
            }

            replies.removeAt(2) // remova o último
            replies.add(
                PantufaReply(
                    content = "**API Ping:** `${System.currentTimeMillis() - time}ms`",
                    prefix = ":zap:",
                    mentionUser = false
                )
            )

            message.editMessage {
                content = replies.joinToString(separator = "\n", transform = { it.build(context) })
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}