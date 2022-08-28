package net.perfectdreams.dreamcorreios.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcorreios.DreamCorreios
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.persistence.PersistentDataType

class CorreiosTransformCaixaPostalExecutor(val m: DreamCorreios) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val block = player.getTargetBlock(10)
        if (block?.type != Material.CHEST)
            context.fail("Não é um baú!")
        val state = block.state as Chest
        state.persistentDataContainer.set(DreamCorreios.IS_CAIXA_POSTAL, PersistentDataType.BYTE, 1)
        state.update()

        context.sendMessage("O baú virou uma caixa postal!")
    }
}