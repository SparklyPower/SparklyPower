package net.perfectdreams.dreamraffle.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamraffle.commands.RaffleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.BuyRaffleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.RaffleScheduleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.RaffleStatsExecutor

object RaffleCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("rifa")) {
        executor = RaffleExecutor

        subcommand(listOf("comprar")) {
            executor = BuyRaffleExecutor
        }

        subcommand(listOf("cronograma")) {
            executor = RaffleScheduleExecutor
        }

        subcommand(listOf("stats")) {
            executor = RaffleStatsExecutor
        }
    }
}