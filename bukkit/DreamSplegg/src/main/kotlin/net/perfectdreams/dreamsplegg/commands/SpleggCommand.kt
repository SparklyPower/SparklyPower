package net.perfectdreams.dreamsplegg.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamsplegg.DreamSplegg

object SpleggCommand : DSLCommandBase<DreamSplegg> {
    override fun command(plugin: DreamSplegg) = create(listOf("splegg")) {
        permission = "dreamsplegg.joinevent"

        executes {
            if (plugin.splegg.isStarted && !plugin.splegg.isPreStart) {
                player.sendMessage("${DreamSplegg.PREFIX} §cO Splegg já começou!")
                return@executes
            }

            if (!plugin.splegg.isStarted && !plugin.splegg.isPreStart) {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamSplegg.PREFIX} §cO Splegg não está ocorrendo no momento!")
                return@executes
            }

            if (player in plugin.splegg.playersInQueue) {
                player.sendMessage("${DreamSplegg.PREFIX} §cVocê já está na fila do Splegg!")
                return@executes
            }

            if (plugin.splegg.isServerEvent) {
                player.sendMessage("${DreamSplegg.PREFIX} §eVocê entrou no evento do Splegg!")
                plugin.splegg.joinQueue(player)
            } else {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamSplegg.PREFIX} §cO Evento Splegg não está ocorrendo no momento!")
                return@executes
            }
        }
    }
}