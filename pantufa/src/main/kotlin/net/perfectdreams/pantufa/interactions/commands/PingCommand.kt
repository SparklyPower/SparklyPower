package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.pantufa.utils.styled

class PingCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("ping", "Pong! \uD83C\uDFD3") {
        executor = PingExecutor()
    }

    inner class PingExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.reply(false) {
                styled("Pong! \uD83C\uDFD3")
            }
        }
    }
}