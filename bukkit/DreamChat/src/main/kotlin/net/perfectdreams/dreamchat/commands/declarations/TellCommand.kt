package net.perfectdreams.dreamchat.commands.declarations

import net.perfectdreams.dreamchat.commands.TellExecutor
import net.perfectdreams.dreamchat.commands.TellLockExecutor
import net.perfectdreams.dreamchat.commands.TellUnlockExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object TellCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("tell", "msg", "pm", "m", "whisper", "w")) {
        subcommand(listOf("lock")) {
            executor = TellLockExecutor
        }

        subcommand(listOf("unlock")) {
            executor = TellUnlockExecutor
        }

        executor = TellExecutor
    }
}