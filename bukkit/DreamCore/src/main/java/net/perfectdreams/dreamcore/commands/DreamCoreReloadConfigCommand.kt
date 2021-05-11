package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import java.io.File

object DreamCoreReloadConfigCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore config reload")) {
        permission = "dreamcore.setup"

        executes {
            plugin.loadConfig()

            sender.sendMessage("Configuração recarregada!")
        }
    }
}