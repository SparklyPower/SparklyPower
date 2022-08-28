package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class TellUnlockExecutor(val m: DreamChat) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        if (m.lockedTells.containsKey(context.sender)) {
            context.sender.sendMessage("§aSeu chat travado com §b${m.lockedTells[context.sender]}§a foi desativado")
            m.lockedTells.remove(context.sender)
            return
        } else {
            context.fail("§cVocê não tem nenhum tell travado para destravar!")
        }
    }
}
