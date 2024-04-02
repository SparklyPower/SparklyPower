package net.perfectdreams.dreamquickharvest.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import net.perfectdreams.dreamquickharvest.commands.ColheitaExecutor
import net.perfectdreams.dreamquickharvest.commands.ColheitaUpgradeExecutor
import net.perfectdreams.dreamquickharvest.commands.DreamQuickHarvestPlayerDeleteUpgradesExecutor
import net.perfectdreams.dreamquickharvest.commands.DreamQuickHarvestPlayerUpgradesListExecutor

class DreamQuickHarvestCommand(val m: DreamQuickHarvest) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamquickharvest")) {
        permission = "dreamquickharvest.setup"

        subcommand(listOf("upgrades")) {
            subcommand(listOf("list")) {
                executor = DreamQuickHarvestPlayerUpgradesListExecutor(m)
            }
            subcommand(listOf("delete")) {
                executor = DreamQuickHarvestPlayerDeleteUpgradesExecutor(m)
            }
        }
    }
}