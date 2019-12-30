package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class QueroTrabalharCommand(val m: DreamMini) : SparklyCommand(arrayOf("querotrabalhar"), permission = "dreammini.querotrabalhar") {

    @Subcommand
    fun queroTrabalhar(sender: Player) {
        if (m.queroTrabalhar.contains(sender)) {
            m.queroTrabalhar.remove(sender)
            sender.sendMessage("§aVocê saiu do modo trabalhar!")
        } else {
            m.queroTrabalhar.add(sender)
            sender.sendMessage("§aVocê agora está no modo trabalhar!")
            sender.sendMessage("")
            sender.sendMessage("§7Use o §6/querotrabalhar§7 apenas quando for necessário, você é da Staff, seu trabalho é ajudar os players!")
        }
    }
}