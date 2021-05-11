package net.perfectdreams.dreamlabirinto.commands

import net.perfectdreams.dreamcore.utils.blacklistedTeleport
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamlabirinto.DreamLabirinto

object LabirintoCommand : DSLCommandBase<DreamLabirinto> {
    override fun command(plugin: DreamLabirinto) = create(listOf("labirinto")) {
        executes {
            if (!plugin.event.running) {
                sender.sendMessage("${DreamLabirinto.PREFIX} Atualmente não há nenhum evento Labirinto acontecendo!")
                return@executes
            }

            if (plugin.event.wonPlayers.contains(player.uniqueId)) {
                sender.sendMessage("${DreamLabirinto.PREFIX}§c Você já venceu o labirinto atual!")
                return@executes
            }

            if (player.location.blacklistedTeleport) {
                sender.sendMessage("${DreamLabirinto.PREFIX} Você está em uma localização que o sistema de GPS não consegue te encontrar!")
                return@executes
            }

            plugin.event.join(player)
        }
    }
}