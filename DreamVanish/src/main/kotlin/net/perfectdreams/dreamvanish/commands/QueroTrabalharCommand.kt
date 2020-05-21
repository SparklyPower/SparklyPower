package net.perfectdreams.dreamvanish.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamvanish.DreamVanish
import net.perfectdreams.dreamvanish.DreamVanishAPI

object QueroTrabalharCommand : DSLCommandBase<DreamVanish> {
    override fun command(plugin: DreamVanish) = create(
        listOf("querotrabalhar")
    ) {
        permission = "dreamvanish.querotrabalhar"

        executes {
            if (DreamVanishAPI.isQueroTrabalhar(player)) {
                DreamVanishAPI.queroTrabalharPlayers.remove(player)
                sender.sendMessage("§aVocê saiu do modo trabalhar!")
            } else {
                DreamVanishAPI.queroTrabalharPlayers.add(player)
                sender.sendMessage("§aVocê agora está no modo trabalhar!")
                sender.sendMessage("")
                sender.sendMessage("§7Use o §6/querotrabalhar§7 apenas quando for necessário, você é da Staff, seu trabalho é ajudar os players!")
            }
        }
    }
}