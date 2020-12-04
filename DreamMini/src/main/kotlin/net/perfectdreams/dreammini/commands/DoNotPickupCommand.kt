package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class DoNotPickupCommand(val m: DreamMini) : SparklyCommand(arrayOf("nãopegar", "naopegar", "donotpickup", "filtrardrops", "np")) {
    @Subcommand
    fun doNotPickup(player: Player) {
        val inventory = m.dropsBlacklist.getOrPut(player) { Bukkit.createInventory(null, 54, "§5§lQuais itens §4§lNÃO§5§l serão pegos?") }

        player.openInventory(inventory)
    }
}