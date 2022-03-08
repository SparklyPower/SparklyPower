package net.perfectdreams.dreamtorredamorte.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte

object TorreCommand : DSLCommandBase<DreamTorreDaMorte> {
    override fun command(plugin: DreamTorreDaMorte) = create(listOf("torre", "torredamorte")) {
        permission = "dreamtorredamorte.joinevent"

        executes {
            if (plugin.torreDaMorte.isStarted && !plugin.torreDaMorte.isPreStart) {
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cA Torre da Morte já começou!")
                return@executes
            }

            if (!plugin.torreDaMorte.isStarted && !plugin.torreDaMorte.isPreStart) {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento!")
                return@executes
            }

            if (plugin.torreDaMorte.isServerEvent) {
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §eVocê entrou no evento da Torre da Morte!")
                plugin.torreDaMorte.joinQueue(player)
            } else {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento!")
                return@executes
            }
        }
    }
}