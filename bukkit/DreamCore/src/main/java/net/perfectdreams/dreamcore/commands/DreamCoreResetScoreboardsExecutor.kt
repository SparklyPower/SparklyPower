package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamCoreResetScoreboardsExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("§eResetando todas as scoreboards... As scoreboards serão recriadas após serem deletadas!")

        plugin.scoreboardManager.resetAllScoreboards()

        context.sendMessage("§aScoreboards resetadas!")
    }
}