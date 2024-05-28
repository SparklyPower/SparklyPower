package net.perfectdreams.pantufa.interactions.commands.say

import net.perfectdreams.discordinteraktions.common.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.messageCommand
import net.perfectdreams.pantufa.PantufaBot

class SayEditMessageCommand(val m: PantufaBot) : MessageCommandDeclarationWrapper {
    override fun declaration() = messageCommand("Editar Mensagem", SayEditMessageExecutor(m))
}