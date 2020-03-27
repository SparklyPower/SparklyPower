package net.perfectdreams.dreamtorredamorte.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import net.perfectdreams.dreamtorredamorte.utils.TorreDaMorte
import org.bukkit.entity.Player

class TorreCommand(val m: DreamTorreDaMorte) : SparklyCommand(arrayOf("torre", "dreamtorredamorte.use")) {
    @Subcommand
    fun torre(player: Player) {
        if (m.torreDaMorte.isStarted && !m.torreDaMorte.isPreStart) {
            player.sendMessage("Já está acontecendo, very sad né")
            return
        }

        if (!m.torreDaMorte.isPreStart && !m.torreDaMorte.isStarted) {
            m.torreDaMorte.preStart()
        }

        player.sendMessage("vixe rsrs entrou na partida")
        m.torreDaMorte.joinQueue(player)
    }
}