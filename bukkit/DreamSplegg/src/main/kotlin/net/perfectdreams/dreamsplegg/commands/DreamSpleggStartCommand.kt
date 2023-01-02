package net.perfectdreams.dreamsplegg.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamsplegg.DreamSplegg

object DreamSpleggStartCommand : DSLCommandBase<DreamSplegg> {
    override fun command(plugin: DreamSplegg) = create(listOf("dreamsplegg start")) {
        permission = "dreamsplegg.setup"

        executes {
            plugin.eventoSplegg.preStart()
        }
    }
}