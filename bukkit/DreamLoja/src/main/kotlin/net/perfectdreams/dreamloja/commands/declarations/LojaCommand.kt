package net.perfectdreams.dreamloja.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamloja.commands.DeleteLojaExecutor
import net.perfectdreams.dreamloja.commands.LojaExecutor
import net.perfectdreams.dreamloja.commands.SetLojaExecutor
import net.perfectdreams.dreamloja.commands.SetLojaIconExecutor

object LojaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("loja")) {
        subcommand(listOf("set")) {
            executor = SetLojaExecutor
        }

        subcommand(listOf("icon")) {
            executor = SetLojaIconExecutor
        }

        subcommand(listOf("delete")) {
            executor = DeleteLojaExecutor
        }

        executor = LojaExecutor
    }
}