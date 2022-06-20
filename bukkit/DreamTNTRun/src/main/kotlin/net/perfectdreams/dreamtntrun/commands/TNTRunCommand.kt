package net.perfectdreams.dreamtntrun.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamtntrun.DreamTNTRun

object TNTRunCommand : DSLCommandBase<DreamTNTRun> {
    override fun command(plugin: DreamTNTRun) = create(listOf("tntrun")) {
        permission = "dreamtntrun.joinevent"

        executes {
            if (plugin.TNTRun.isStarted && !plugin.TNTRun.isPreStart) {
                player.sendMessage("${DreamTNTRun.PREFIX} §cO TNT Run já começou!")
                return@executes
            }

            if (!plugin.TNTRun.isStarted && !plugin.TNTRun.isPreStart) {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamTNTRun.PREFIX} §cO Evento TNT Run não está ocorrendo no momento!")
                return@executes
            }

            if (player in plugin.TNTRun.playersInQueue) {
                player.sendMessage("${DreamTNTRun.PREFIX} §cVocê já está na fila do TNT Run!")
                return@executes
            }

            if (plugin.TNTRun.isServerEvent) {
                player.sendMessage("${DreamTNTRun.PREFIX} §eVocê entrou no evento do TNT Run!")
                plugin.TNTRun.joinQueue(player)
            } else {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                player.sendMessage("${DreamTNTRun.PREFIX} §cO Evento TNT Run não está ocorrendo no momento!")
                return@executes
            }
        }
    }
}