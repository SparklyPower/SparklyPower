package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.OnlineExecutor
import net.perfectdreams.pantufa.interactions.commands.say.SaySendExecutor

class SayCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "say",
        description = "Envia mensagens pela Pantufa"
    ) {
        subcommand("send", "Envia uma mensagem pela Pantufa") {
            executor = SaySendExecutor(m)
        }
    }
}