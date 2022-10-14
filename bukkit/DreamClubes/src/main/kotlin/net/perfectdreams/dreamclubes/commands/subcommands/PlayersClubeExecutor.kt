package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit

class PlayersClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            val members = onAsyncThread { clube.retrieveMembers() }

            player.sendMessage("§8[ §bMembros §8]".centralizeHeader())
            player.sendMessage("§6Quantidade de Membros: §e" + members.size)
            player.sendMessage("")
            val tg = TableGenerator(
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER
            )
            tg.addRow("§8Rank", "§8Nome§r", "§8KDR§r")
            tg.addRow()

            for (wrapper in members.sortedByDescending { it.permissionLevel.weight }) {
                val offlinePlayer = Bukkit.getOfflinePlayer(wrapper.id.value)
                val name = wrapper.permissionLevel.name
                val kdr = onAsyncThread { ClubeAPI.getPlayerKD(wrapper.id.value) }
                tg.addRow("§6$name", "§b" + offlinePlayer.name, "§f" + kdr.getRatio())
            }

            for (line in tg.generate(TableGenerator.Receiver.CLIENT, true, true)) {
                player.sendMessage(line)
            }

            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }
}