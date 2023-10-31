package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import org.bukkit.Bukkit

class DreamCoreScoreboardStatsExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("§eScoreboards: ${plugin.scoreboardManager.scoreboards.size} (Players Online: ${Bukkit.getOnlinePlayers().size})")
    }
}