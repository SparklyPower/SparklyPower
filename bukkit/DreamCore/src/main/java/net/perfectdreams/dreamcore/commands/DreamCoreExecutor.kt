package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration

class DreamCoreExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamCoreExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("§aDreamCore! Let's make the world a better place, one plugin at a time")
    }
}