package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamChatStartCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("dreamchat start")) {
        permission = "dreamchat.setup"

        executes {
            plugin.eventoChat.preStart()
        }
    }
}