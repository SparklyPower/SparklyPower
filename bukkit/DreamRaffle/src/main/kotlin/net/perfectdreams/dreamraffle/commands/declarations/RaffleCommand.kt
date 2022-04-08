package net.perfectdreams.dreamraffle.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamraffle.commands.RaffleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.BuyRaffleExecutor

object RaffleCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("rifa")) {
        executor = RaffleExecutor

        subcommand(listOf("comprar")) {
            executor = BuyRaffleExecutor
        }
    }
}