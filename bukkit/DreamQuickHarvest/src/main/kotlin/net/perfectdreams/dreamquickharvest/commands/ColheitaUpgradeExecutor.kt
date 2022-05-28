package net.perfectdreams.dreamquickharvest.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest

class ColheitaUpgradeExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(ColheitaUpgradeExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val colheitaInfo = m.getOrCreateQuickHarvestInfo(player)
    }
}