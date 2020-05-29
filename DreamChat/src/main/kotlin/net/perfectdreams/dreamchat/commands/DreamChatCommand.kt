package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamChatCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("dreamchat")) {
        permission = "dreamchat.setup"

        executes {
            sender.sendMessage("§6/dreamchat start")
            sender.sendMessage("§6/dreamchat tellon")
            sender.sendMessage("§6/dreamchat telloff")
        }
    }
}