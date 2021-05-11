package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamChatReloadCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("dreamchat reload")) {
        permission = "dreamchat.setup"

        executes {
            plugin.reload()

            sender.sendMessage("Reload concluido!")
        }
    }
}