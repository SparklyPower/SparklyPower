package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader

class KDRExecutor(val m: DreamClubes) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val kdr = ClubeAPI.getPlayerKD(player.uniqueId)

            player.sendMessage("§8[ §bKDR §8]".centralizeHeader())
            player.sendMessage("§eKills: §6" + kdr.kills)
            player.sendMessage("§eMortes: §6" + kdr.deaths)
            player.sendMessage("§eKDR: §6" + kdr.getRatio())
            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }
}