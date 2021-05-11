package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player


class PlayersSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        async {
            val members = clube.retrieveMembers()

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
                val kdr = ClubeAPI.getPlayerKD(wrapper.id.value)
                tg.addRow("§6$name", "§b" + offlinePlayer.name, "§f" + kdr.getRatio())
            }

            for (line in tg.generate(TableGenerator.Receiver.CLIENT, true, true)) {
                player.sendMessage(line)
            }

            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }
}