package net.perfectdreams.dreamminarecheada.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamminarecheada.DreamMinaRecheada
import org.bukkit.entity.Player
import java.io.File

class MinaRecheadaCommand(internal var m: DreamMinaRecheada) : SparklyCommand(arrayOf("minarecheada", "mina")) {
    @Subcommand(["setspawn"])
    @SubcommandPermission("sparklyminarecheada.setup")
    fun setSpawn(player: Player) {
        m.minaRecheada.minaRecheadaData.spawn = player.location
        player.sendMessage("§eSpawn marcado!")
        save()
    }

    @Subcommand(["setpos1"])
    @SubcommandPermission("sparklyminarecheada.setup")
    fun setPos1(player: Player) {
        m.minaRecheada.minaRecheadaData.pos1 = player.location
        player.sendMessage("§ePos1 marcado!")
        save()
    }

    @Subcommand(["setpos2"])
    @SubcommandPermission("sparklyminarecheada.setup")
    fun setPos2(player: Player) {
        m.minaRecheada.minaRecheadaData.pos2 = player.location
        player.sendMessage("§ePos2 marcado!")
        save()
    }

    @Subcommand(["forcestart"])
    @SubcommandPermission("sparklyminarecheada.setup")
    fun startMina(player: Player) {
        m.minaRecheada.preStart()
        player.sendMessage("§eIniciando Mina Recheada")
    }

    @Subcommand
    fun goToMina(player: Player) {
        if (m.minaRecheada.running) {
            player.sendMessage(DreamMinaRecheada.PREFIX + "§aVocê entrou na Mina Recheada, divirta-se!")
            player.teleport(m.minaRecheada.minaRecheadaData.spawn)
        } else {
            player.sendMessage(DreamMinaRecheada.PREFIX + "§cAtualmente há nenhum Evento Mina Recheada acontecendo...")
        }
    }

    fun save() {
        File(m.dataFolder, "minarecheadadata.json").writeText(DreamUtils.gson.toJson(m.minaRecheada.minaRecheadaData))
    }
}
