package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File

class DreamCoreReloadExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamCoreReloadExecutor::class) {
        object Options : CommandOptions() {
            val fileName = greedyString("filename")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val sender = context.sender
        val fileName = args[options.fileName]

        if (fileName == "all") {
            sender.sendMessage("§aRecarregando TODOS os scripts!")
            plugin.dreamScriptManager.unloadScripts()
            plugin.dreamScriptManager.loadScripts()
            sender.sendMessage("§aProntinho! ${plugin.dreamScriptManager.scripts.size} scripts foram carregados ^-^")
        } else {
            val script = plugin.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
            sender.sendMessage("§aRecarregando script $fileName!")
            if (script != null)
                plugin.dreamScriptManager.unloadScript(script)
            try {
                plugin.dreamScriptManager.loadScript(File(plugin.dataFolder, "scripts/$fileName"), true)
                sender.sendMessage("§aProntinho! $fileName foi carregado com sucesso!")
            } catch (e: Exception) {
                sender.sendMessage("§cAlgo deu errado ao carregar $fileName! ${e.message}")
            }
        }
    }
}