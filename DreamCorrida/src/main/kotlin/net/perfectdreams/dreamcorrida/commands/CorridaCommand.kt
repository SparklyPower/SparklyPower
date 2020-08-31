package net.perfectdreams.dreamcorrida.commands

import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.blacklistedTeleport
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import net.perfectdreams.dreamcorrida.DreamCorrida
import net.perfectdreams.dreamcorrida.utils.Checkpoint
import net.perfectdreams.dreamcorrida.utils.Corrida
import net.perfectdreams.dreamcorrida.utils.toWrapper
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import java.io.File

class CorridaCommand(val m: DreamCorrida) : SparklyCommand(arrayOf("corrida")) {
    @Subcommand
    fun root(sender: Player) {
        if (!m.eventoCorrida.running) {
            sender.sendMessage("${DreamCorrida.PREFIX} Atualmente não há nenhum evento Corrida acontecendo!")
            return
        }

        if (sender.location.blacklistedTeleport) {
            sender.sendMessage("${DreamCorrida.PREFIX} Você está em uma localização que o sistema de GPS não consegue te encontrar!")
            return
        }

        if (m.eventoCorrida.wonPlayers.contains(sender.uniqueId)) {
            sender.sendMessage("${DreamCorrida.PREFIX}§c Você já venceu a corrida atual!")
            return
        }

        val spawnLocation = m.eventoCorrida.corrida?.spawn?.toLocation()!!

        sender.teleport(spawnLocation)
        sender.removeAllPotionEffects()

        sender.playSound(spawnLocation, "perfectdreams.sfx.special_stage", SoundCategory.RECORDS, 1000f, 1f)
    }

    @Subcommand(["start"])
    @SubcommandPermission("dreamcorrida.manage")
    fun start(sender: Player) {
        if (m.eventoCorrida.running) {
            sender.sendMessage("${DreamCorrida.PREFIX} Já existe um evento Corrida acontecendo!")
            return
        }

        m.eventoCorrida.preStart()
    }

    @Subcommand(["create"])
    @SubcommandPermission("dreamcorrida.manage")
    fun setSpawn(sender: Player, corridaName: String) {
        m.availableCorridas.add(
            Corrida(
                corridaName,
                sender.location.toWrapper()
            )
        )

        sender.sendMessage("§aCorrida $corridaName criada com sucesso!")
    }

    @Subcommand(["add_checkpoint"])
    @SubcommandPermission("dreamcorrida.manage")
    fun addCheckpoint(sender: Player, corridaName: String, regionName: String, fancyName: Array<String>) {
        val corrida = m.availableCorridas.first { it.name == corridaName }
        corrida.checkpoints.add(
            Checkpoint(
                regionName,
                fancyName.joinToString(" "),
                sender.location.toWrapper()
            )
        )

        sender.sendMessage("§aCheckpoint $regionName criado com sucesso!")
    }

    @Subcommand(["remove_checkpoint"])
    @SubcommandPermission("dreamcorrida.manage")
    fun removeCheckpoint(sender: Player, corridaName: String, regionName: String) {
        val corrida = m.availableCorridas.first { it.name == corridaName }
        corrida.checkpoints.removeIf { it.regionName == regionName }

        sender.sendMessage("§aCheckpoint $regionName removido com sucesso!")
    }

    @Subcommand(["ready"])
    @SubcommandPermission("dreamcorrida.manage")
    fun setCorridaAsReady(sender: Player, corridaName: String) {
        val corrida = m.availableCorridas.first { it.name == corridaName }
        corrida.ready = !corrida.ready

        sender.sendMessage("§aCheckpoint $corridaName está pronta? ${corrida.ready}")
    }

    @Subcommand(["save"])
    @SubcommandPermission("dreamcorrida.manage")
    fun saveCorrida(sender: Player, corridaName: String) {
        val corrida = m.availableCorridas.first { it.name == corridaName }

        val corridaFolders = File(m.dataFolder, "corridas")
        val corridaFile = File(corridaFolders, "$corridaName.json")
        corridaFile.writeText(
            DreamUtils.gson.toJson(corrida)
        )

        sender.sendMessage("§aCorrida $corridaName salva com sucesso!")
    }

    @Subcommand(["delete"])
    @SubcommandPermission("dreamcorrida.manage")
    fun deleteCorrida(sender: Player, corridaName: String) {
        m.availableCorridas.removeIf { it.name == corridaName }

        sender.sendMessage("§aCorrida $corridaName deletada com sucesso!")
    }
}