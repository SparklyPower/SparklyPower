package net.perfectdreams.dreamtorredamorte.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte

object DreamTorreDaMorteStartCommand : DSLCommandBase<DreamTorreDaMorte> {
    override fun command(plugin: DreamTorreDaMorte) = create(listOf("dreamtorredamorte start")) {
        permission = "dreamtorredamorte.setup"

        executes {
            plugin.eventoTorreDaMorte.preStart()
        }
    }
}