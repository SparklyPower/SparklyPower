package net.perfectdreams.dreamraffle.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamraffle.commands.DreamRaffleExecutor

object DreamRaffleCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamraffle")) {
        subcommand(listOf("terminar")) {
            permissions = listOf("dreamraffle.finish")
            executor = DreamRaffleExecutor
        }
    }
}