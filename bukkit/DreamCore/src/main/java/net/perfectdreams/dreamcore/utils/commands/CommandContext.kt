package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandContext(val sender: CommandSender, val label: String, val command: Command<CommandContext>, val args: Array<String>) {
    val player: Player
        get() = if (sender is Player) sender else throw CommandException("§cApenas players podem utilizar este comando!")
}