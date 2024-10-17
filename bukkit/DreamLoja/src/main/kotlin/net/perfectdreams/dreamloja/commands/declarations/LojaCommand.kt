package net.perfectdreams.dreamloja.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.commands.*

class LojaCommand(val m: DreamLoja) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("loja")) {
        subcommand(listOf("manage")) {
            subcommand(listOf("set")) {
                executor = SetLojaExecutor(m)
            }

            subcommand(listOf("icon")) {
                executor = SetLojaIconExecutor(m)
            }

            subcommand(listOf("delete")) {
                executor = DeleteLojaExecutor(m)
            }

            subcommand(listOf("list")) {
                executor = LojaListExecutor(m)
            }

            subcommand(listOf("order")) {
                executor = LojaOrderExecutor(m)
            }

            subcommand(listOf("rename")) {
                executor = RenameLojaExecutor(m)
            }

            executor = LojaManageExecutor(m)
        }

        executor = LojaExecutor(m)
    }
}