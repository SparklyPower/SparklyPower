package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.commands.TellExecutor.Companion.Options.receiver
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamvanish.DreamVanishAPI

class TellUnlockExecutor(val m: DreamChat) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(TellUnlockExecutor::class)

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
