package net.perfectdreams.dreamajuda.commands

import net.perfectdreams.dreamajuda.DreamAjuda
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.block.Sign

class AjudaExecutor(val m: DreamAjuda) : SparklyCommandExecutor() {
    companion object CorreiosExecutor : SparklyCommandExecutorDeclaration(AjudaExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        m.openMenu(context.requirePlayer())
    }
}