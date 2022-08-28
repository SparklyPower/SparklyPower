package net.perfectdreams.dreamchat.commands.declarations

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.commands.TellExecutor
import net.perfectdreams.dreamchat.commands.TellLockExecutor
import net.perfectdreams.dreamchat.commands.TellUnlockExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class TellCommand(val m: DreamChat) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("tell", "msg", "pm", "m", "whisper", "w")) {
        subcommand(listOf("lock")) {
            executor = TellLockExecutor(m)
        }

        subcommand(listOf("unlock")) {
            executor = TellUnlockExecutor(m)
        }

        executor = TellExecutor(m)
    }
}