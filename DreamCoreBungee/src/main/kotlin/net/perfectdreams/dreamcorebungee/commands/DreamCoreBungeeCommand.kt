package net.perfectdreams.dreamcorebungee.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent

class DreamCoreBungeeCommand : SparklyBungeeCommand(arrayOf("dreamcorebungee")) {

    @Subcommand
    suspend fun root(sender: CommandSender) {
        sender.sendMessage("Hello, World!".toTextComponent())
    }
}