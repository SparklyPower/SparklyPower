package net.perfectdreams.dreamquickharvest.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamquickharvest.commands.ColheitaExecutor
import net.perfectdreams.dreamquickharvest.commands.ColheitaUpgradeExecutor

object ColheitaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("colheita")) {
        executor = ColheitaExecutor

        subcommand(listOf("upgrade")) {
            executor = ColheitaUpgradeExecutor
        }
    }
}