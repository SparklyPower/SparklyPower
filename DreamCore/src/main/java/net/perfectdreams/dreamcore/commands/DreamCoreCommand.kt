package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamCoreCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore")) {
        permission = "dreamcore.setup"

        executes {
            sender.sendMessage("§aDreamCore! Let's make the world a better place, one plugin at a time")
        }
    }
}