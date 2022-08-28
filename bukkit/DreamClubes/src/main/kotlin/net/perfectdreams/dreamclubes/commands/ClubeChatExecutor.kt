package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit

class ClubeChatExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val message = greedyString("message")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val message = args[options.message]

        withPlayerClube(player) { clube, clubeMember ->
            val permissionLevel = clubeMember.permissionLevel
            val customPrefix = clubeMember.customPrefix
            var tag = permissionLevel.tagName

            if (customPrefix != null)
                tag = "$tag/${customPrefix.translateColorCodes()}"

            val members = onAsyncThread {
                clube.retrieveMembers()
            }

            val onlineMembers = members.mapNotNull { Bukkit.getPlayer(it.id.value) }
            onlineMembers.forEach { clubeOnlinePlayer ->
                clubeOnlinePlayer.sendMessage("§6$tag §3${player.displayName} §6» §x§0§0§d§d§f§f$message")
            }

            for (staff in Bukkit.getOnlinePlayers().asSequence().filter { it.hasPermission("dreamclubes.snoop") }) {
                staff.sendMessage("§7[${player.name} » Clube ${clube.cleanShortName}] $message")
            }

            if (onlineMembers.size == 1) {
                player.sendMessage("§cSomente você do seu clube está online!")
            }
        }
    }
}