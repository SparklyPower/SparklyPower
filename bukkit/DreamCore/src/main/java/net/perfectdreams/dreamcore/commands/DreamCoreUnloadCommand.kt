package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import java.io.File

object DreamCoreUnloadCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore unload")) {
        permission = "dreamcore.setup"

        executes {
            val fileName = this.args.getOrNull(0) ?: return@executes

            if (fileName == "all") {
                sender.sendMessage("§Descarregando TODOS os scripts!")
                plugin.dreamScriptManager.unloadScripts()
                sender.sendMessage("§aProntinho! Todos os scripts foram descarregados ^-^")
            } else {
                val script = plugin.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
                if (script == null) {
                    sender.sendMessage("§cO script ${fileName} não existe! Use reload seu tosco!")
                    return@executes
                }

                sender.sendMessage("§aDescarregando script $fileName!")
                plugin.dreamScriptManager.unloadScript(script)
                sender.sendMessage("§aProntinho! $fileName foi descarregado com sucesso!")
            }
        }
    }
}