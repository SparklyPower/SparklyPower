package net.perfectdreams.dreamtorredamorte.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte

object TorreMinigameCommand : DSLCommandBase<DreamTorreDaMorte> {
    override fun command(plugin: DreamTorreDaMorte) = create(listOf("torre minigame", "torredamorte minigame")) {
        permission = "dreamtorredamorte.joinminigame"

        executes {
            if (plugin.torreDaMorte.isStarted && !plugin.torreDaMorte.isPreStart) {
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cA Torre da Morte já começou!")
                return@executes
            }

            if (!plugin.torreDaMorte.isPreStart && !plugin.torreDaMorte.isStarted) {
                plugin.torreDaMorte.preStart(false)
            }

            player.sendMessage("${DreamTorreDaMorte.PREFIX} §eVocê entrou na partida do minigame da Torre da Morte!")
            plugin.torreDaMorte.joinQueue(player)
        }
    }
}