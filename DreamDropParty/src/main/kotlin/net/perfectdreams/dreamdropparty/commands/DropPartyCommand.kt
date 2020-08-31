package net.perfectdreams.dreamdropparty.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.blacklistedTeleport
import net.perfectdreams.dreamdropparty.DreamDropParty
import net.perfectdreams.dreamdropparty.utils.toWrapper
import org.bukkit.entity.Player
import java.io.File

class DropPartyCommand(val m: DreamDropParty) : SparklyCommand(arrayOf("dropparty", "festadafirma")) {
    @Subcommand
    fun root(sender: Player) {
        if (!m.dropParty.running) {
            sender.sendMessage("${DreamDropParty.PREFIX} Atualmente não há nenhum evento Festa da Firma acontecendo!")
            return
        }

        if (sender.location.blacklistedTeleport) {
            sender.sendMessage("${DreamDropParty.PREFIX} Você está em uma localização que o sistema de GPS não consegue te encontrar!")
            return
        }

        sender.teleport(m.config.spawn.toLocation())
    }

    @Subcommand(["start"])
    @SubcommandPermission("dreamdropparty.manage")
    fun start(sender: Player) {
        if (m.dropParty.running) {
            sender.sendMessage("${DreamDropParty.PREFIX} Já existe um evento Festa da Firma acontecendo!")
            return
        }

        m.dropParty.preStart()
    }

    @Subcommand(["set_spawn", "setspawn"])
    @SubcommandPermission("dreamdropparty.manage")
    fun setSpawn(sender: Player) {
        m.config.spawn = sender.location.toWrapper()

        val file = File(m.dataFolder, "config.json")
        file.writeText(DreamUtils.gson.toJson(m.config))
        sender.sendMessage("uwu")
    }
}