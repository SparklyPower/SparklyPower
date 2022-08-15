package net.perfectdreams.dreamajuda.commands.declarations

import net.perfectdreams.dreamajuda.commands.AjudaExecutor
import net.perfectdreams.dreamajuda.commands.TransformRulesSignExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object AjudaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("ajuda")) {
        executor = AjudaExecutor
    }
}