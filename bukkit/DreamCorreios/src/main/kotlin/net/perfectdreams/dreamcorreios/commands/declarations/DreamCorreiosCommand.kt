package net.perfectdreams.dreamcorreios.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcorreios.commands.CorreiosGiveExecutor
import net.perfectdreams.dreamcorreios.commands.CorreiosOpenExecutor
import net.perfectdreams.dreamcorreios.commands.CorreiosTransformCaixaPostalExecutor

object DreamCorreiosCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamcorreios")) {
        subcommand(listOf("give")) {
            permissions = listOf("dreamcorreios.setup")
            executor = CorreiosGiveExecutor
        }

        subcommand(listOf("open")) {
            permissions = listOf("dreamcorreios.setup")
            executor = CorreiosOpenExecutor
        }

        subcommand(listOf("caixapostal")) {
            permissions = listOf("dreamcorreios.setup")

            subcommand(listOf("transform")) {
                permissions = listOf("dreamcorreios.setup")
                executor = CorreiosTransformCaixaPostalExecutor
            }
        }
    }
}