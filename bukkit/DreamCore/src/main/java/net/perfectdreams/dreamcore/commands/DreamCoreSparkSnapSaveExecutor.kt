package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamCoreSparkSnapSaveExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val sparkSnap = plugin.sparkSnap
        if (sparkSnap == null) {
            context.sendMessage("§cSparkSnap não está ativo! O spark está instalado no servidor?")
            return
        }

        context.sendMessage("§aSalvando snapshot do profiler do spark...")
        plugin.launchAsyncThread {
            sparkSnap.snap()
            context.sendMessage("§aSnapshot do profiler do spark foi salvo!")
        }
    }
}