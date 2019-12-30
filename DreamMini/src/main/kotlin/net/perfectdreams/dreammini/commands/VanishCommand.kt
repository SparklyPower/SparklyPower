package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class VanishCommand(val m: DreamMini) : SparklyCommand(arrayOf("vanish"), permission = "dreammini.vanish") {

    @Subcommand
    fun vanish(sender: Player) {
        if (m.vanished.contains(sender)) {
            for (player in m.server.onlinePlayers) {
                player.showPlayer(m, sender)
            }

            m.vanished.remove(sender)
            sender.sendMessage("§aVocê agora está §lvísível§a!")
            return
        }

        for (player in m.server.onlinePlayers) {
            player.hidePlayer(m, sender)
        }

        m.vanished.add(sender)
        sender.sendMessage("§aVocê agora está §linvisível§a!")
    }

}