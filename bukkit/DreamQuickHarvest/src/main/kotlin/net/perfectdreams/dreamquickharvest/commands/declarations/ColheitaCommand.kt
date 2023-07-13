package net.perfectdreams.dreamquickharvest.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import net.perfectdreams.dreamquickharvest.commands.ColheitaExecutor
import net.perfectdreams.dreamquickharvest.commands.ColheitaUpgradeExecutor

class ColheitaCommand(val m: DreamQuickHarvest) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("colheita")) {
        executor = ColheitaExecutor(m)

        subcommand(listOf("upgrade")) {
            executor = ColheitaUpgradeExecutor(m)
        }
    }
}