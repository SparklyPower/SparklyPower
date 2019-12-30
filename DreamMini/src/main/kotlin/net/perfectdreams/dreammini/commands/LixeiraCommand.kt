package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class LixeiraCommand(val m: DreamMini) : SparklyCommand(arrayOf("lixeira", "garbage", "lixo"), permission = "dreammini.lixeira") {

    @Subcommand
    fun lixeira(sender: Player) {
        val inventory = Bukkit.createInventory(null, 54, "§4§lLixeira")

        sender.openInventory(inventory)
    }
}