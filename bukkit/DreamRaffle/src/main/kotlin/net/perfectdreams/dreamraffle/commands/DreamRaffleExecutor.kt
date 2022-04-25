package net.perfectdreams.dreamraffle.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamraffle.tasks.RafflesManager

class DreamRaffleExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamRaffleExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        RafflesManager.end()
    }
}