package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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

                    for (staff in Bukkit.getOnlinePlayers().asSequence().filter { it.hasPermission("dreamclubes.snoop") }) {
                        staff.sendMessage("§7[${player.name} » Clube ${clube.cleanShortName}] $message")
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