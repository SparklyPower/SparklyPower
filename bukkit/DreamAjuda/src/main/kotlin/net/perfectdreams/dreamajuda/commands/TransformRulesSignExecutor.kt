package net.perfectdreams.dreamajuda.commands

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.block.Sign

class TransformRulesSignExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val block = player.getTargetBlock(10)
        if (block == null || !block.type.name.contains("_SIGN"))
            context.fail("Não é uma placa!")
        val state = block.state as Sign
        state.persistentDataContainer.set(DreamAjuda.IS_RULES_SIGN, true)
        state.update()

        context.sendMessage("A placa virou uma placa de regras!")
    }
}