package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamCoreReloadConfigExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.loadConfig()

        context.sender.sendMessage("§aConfiguração recarregada!")
    }
}