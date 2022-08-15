package net.perfectdreams.dreamajuda.commands.declarations

import net.perfectdreams.dreamajuda.commands.TransformRulesSignExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object DreamAjudaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamajuda")) {
        subcommand(listOf("regras")) {
            permissions = listOf("dreamajuda.setup")

            subcommand(listOf("transform")) {
                permissions = listOf("dreamajuda.setup")
                executor = TransformRulesSignExecutor
            }
        }
    }
}