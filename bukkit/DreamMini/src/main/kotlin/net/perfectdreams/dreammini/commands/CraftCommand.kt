package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class CraftCommand(val m: DreamMini) : SparklyCommand(arrayOf("craft", "crafttable", "craftingtable", "crafting"), permission = "dreammini.craft") {
    @Subcommand
    fun craft(player: Player) {
        player.openWorkbench(player.location, true)
    }
}