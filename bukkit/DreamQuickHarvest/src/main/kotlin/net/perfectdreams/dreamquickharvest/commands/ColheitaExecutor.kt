package net.perfectdreams.dreamquickharvest.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest

class ColheitaExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(ColheitaExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val colheitaInfo = m.getOrCreateQuickHarvestInfo(player)
        context.sendMessage("§aAtualmente você tem §e${colheitaInfo.activeBlocks} blocos§a de plantação que você pode quebrar rapidamente")
        context.sendMessage("§aNo total, você pode ter §e${DreamQuickHarvest.MAX_BLOCKS} blocos§a de colheita rápida")
        context.sendMessage("§aVocê ganha §e${m.howManyQuickHarvestBlocksThePlayerShouldEarn(player)} blocos§a de colheita por segundo. Ao subir nível de herbalismo, a sua colheita rápida aumenta mais rápido (Até o Nível 1000)")
        // context.sendMessage("§aVocê pode aumentar o seu limite de blocos de colheita rápida com pesadelos, cada X blocos custa Y pesadelos, e o upgrade dura uma semana!")
    }
}