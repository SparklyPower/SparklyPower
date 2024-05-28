package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.TransactionsExecutor

class TransactionsCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        "transactions",
        "Confira as transações mais recentes com base nos critérios escolhidos"
    ) {
        executor = TransactionsExecutor(m)
    }
}