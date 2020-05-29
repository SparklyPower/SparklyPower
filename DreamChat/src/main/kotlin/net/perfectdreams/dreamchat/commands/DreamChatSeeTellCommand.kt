package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamChatSeeTellCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("dreamchat seetell")) {
        permission = "dreamchat.setup"

        executes {
            if (plugin.hideTells.contains(player)) {
                plugin.hideTells.remove(player)

                sender.sendMessage("§aVocê agora irá mais ver os tells enviados no chat! iiiih tá de olho né safad")
            } else {
                plugin.hideTells.add(player)

                sender.sendMessage("§aVocê agora não irá mais ver os tells enviados no chat! Para reativar, só utilizar o comando novamente!")
            }
        }
    }
}