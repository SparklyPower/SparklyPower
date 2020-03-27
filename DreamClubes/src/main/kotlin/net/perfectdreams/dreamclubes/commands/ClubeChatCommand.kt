package net.perfectdreams.dreamclubes.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import com.sun.jdi.connect.Connector
import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeHome
import net.perfectdreams.dreamclubes.tables.ClubesHomes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class ClubeChatCommand(val m: DreamClubes) : SparklyCommand(arrayOf(".")) {
    @Subcommand
    fun root(player: Player, args: Array<String>) {
        val message = args.joinToString(" ")

        if (message.isBlank()) {
            player.sendMessage("§6/. <mensagem>")
        } else {
            async {
                val clube = ClubeAPI.getPlayerClube(player.uniqueId)

                if (clube != null) {
                    val members = clube.retrieveMembers()
                    toSync()

                    val clubeMember = members.first { it.id.value == player.uniqueId }
                    val permissionLevel = clubeMember.permissionLevel
                    val customPrefix = clubeMember.customPrefix
                    var tag = permissionLevel.tagName

                    if (customPrefix != null)
                        tag = "$tag/${customPrefix.translateColorCodes()}"

                    val onlineMembers = members.mapNotNull { Bukkit.getPlayer(it.id.value) }
                    onlineMembers.forEach { clubeOnlinePlayer ->
                        clubeOnlinePlayer.sendMessage("§6$tag §3${player.displayName} §6» §3$message")
                    }

                    if (onlineMembers.size == 1) {
                        player.sendMessage("§cSomente você do seu clube está online!")
                    }
                } else {
                    player.sendMessage("§cVocê não está em um clube!")
                }
            }
        }
    }
}