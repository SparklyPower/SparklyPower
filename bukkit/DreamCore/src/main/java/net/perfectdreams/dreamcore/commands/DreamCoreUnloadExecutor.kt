package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File

class DreamCoreUnloadExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val fileName = greedyString("filename")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val sender = context.sender
        val fileName = args[options.fileName]

        if (fileName == "all") {
            sender.sendMessage("§Descarregando TODOS os scripts!")
            plugin.dreamScriptManager.unloadScripts()
            sender.sendMessage("§aProntinho! Todos os scripts foram descarregados ^-^")
        } else {
            val script = plugin.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
            if (script == null) {
                sender.sendMessage("§cO script $fileName não existe! Use reload seu tosco!")
                return
            }

            sender.sendMessage("§aDescarregando script $fileName!")
            plugin.dreamScriptManager.unloadScript(script)
            sender.sendMessage("§aProntinho! $fileName foi descarregado com sucesso!")
        }
    }
}