package net.perfectdreams.dreamclubes.commands.subcommands

import org.bukkit.entity.Player

interface SubCommand {
    fun execute(player: Player, args: Array<String>)
}