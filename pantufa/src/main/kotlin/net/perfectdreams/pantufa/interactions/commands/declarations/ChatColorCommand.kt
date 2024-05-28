package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.ChatColorExecutor

class ChatColorCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "chatcolor",
        "Transforme uma cor RGB em uma cor que você possa usar no chat (e em outros lugares) do SparklyPower!"
    ) {
        executor = ChatColorExecutor(m)
    }
}