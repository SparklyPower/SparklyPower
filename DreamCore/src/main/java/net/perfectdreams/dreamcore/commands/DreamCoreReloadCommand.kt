package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import java.io.File

object DreamCoreReloadCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore reload")) {
        permission = "dreamcore.setup"

        executes {
            val fileName = this.args.getOrNull(0) ?: return@executes

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
}