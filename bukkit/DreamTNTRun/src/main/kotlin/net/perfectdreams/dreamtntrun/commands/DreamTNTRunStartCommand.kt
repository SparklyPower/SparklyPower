package net.perfectdreams.dreamtntrun.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamtntrun.DreamTNTRun

object DreamTNTRunStartCommand : DSLCommandBase<DreamTNTRun> {
    override fun command(plugin: DreamTNTRun) = create(listOf("dreamtntrun start")) {
        permission = "dreamtntrun.setup"

        executes {
            plugin.eventoTNTRun.preStart()
        }
    }
}