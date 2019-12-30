package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class PhantomCommand(val m: DreamMini) : SparklyCommand(arrayOf("phantom")) {
    @Subcommand
    fun togglePhantoms(player: Player) {
        if (m.phantomWhitelist.contains(player.uniqueId)) {
            m.phantomWhitelist.remove(player.uniqueId)
            player.sendMessage("§aAgora phantoms estão desativados para você! Ahhhh, como é bom estar calmo sem ter uma cópia barata de Ender Dragon te perseguindo!")
        } else {
            m.phantomWhitelist.add(player.uniqueId)
            player.sendMessage("§aAgora phantoms estão ativados para você! Tome cuidado, caro aventureiro!")
        }
    }
}