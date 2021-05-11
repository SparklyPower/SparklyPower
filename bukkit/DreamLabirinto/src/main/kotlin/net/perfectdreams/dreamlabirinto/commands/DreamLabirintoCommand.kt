package net.perfectdreams.dreamlabirinto.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamlabirinto.DreamLabirinto

object DreamLabirintoCommand : DSLCommandBase<DreamLabirinto> {
    override fun command(plugin: DreamLabirinto) = create(listOf("dreamlabirinto")) {
        permission = "dreamlabirinto.setup"

        executes {
        }
    }
}