package net.perfectdreams.dreamclubes.commands.subcommands

import kotlinx.coroutines.Dispatchers
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.selectAll

class ClubesListaClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val clubesCount = net.perfectdreams.exposedpowerutils.sql.transaction(
                Dispatchers.IO,
                Databases.databaseNetwork
            ) {
                Clubes.selectAll().count()
            }

            player.sendMessage("§8[ §bClubes §8]".centralizeHeader())
            player.sendMessage("§6Quantidade de Clubes: §e" + clubesCount)
            player.sendMessage("")
            val tg = TableGenerator(
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER
            )
            tg.addRow("§8Rank", "§8Nome§r", "§8KDR§r", "§8Membros§r")
            tg.addRow("", "", "", "")

            // TODO: This can be optimized
            val clubesWrapper = net.perfectdreams.exposedpowerutils.sql.transaction(
                Dispatchers.IO,
                Databases.databaseNetwork
            ) {
                Clube.all()
                    .map {
                        val members = it.retrieveMembers()
                        val kdr = members
                            .sumOf {
                                ClubeAPI.getPlayerKD(it.id.value)
                                    .getRatio()
                            }

                        ClubeWrapper(it, members, kdr / members.size.coerceAtLeast(1))
                    }
            }

            val clubesSortedByKDR = clubesWrapper.sortedByDescending { it.kdr }

            var index = 1

            for ((clube, members, kdr) in clubesSortedByKDR.take(10)) {
                var name = "§8«§7§3" + clube.shortName + "§8»§7 " + clube.name
                if (name.length > 50) {
                    name = name.substring(0, 47)
                    name = "$name..."
                }
                tg.addRow(
                    "§6$index.",
                    name,
                    "§c$kdr",
                    "§f" + members.size.toString()
                )
                ++index
            }

            val lines = tg.generate(TableGenerator.Receiver.CLIENT, true, true).iterator()

            for (line in lines) {
                player.sendMessage(line)
            }

            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }

    data class ClubeWrapper(
        val clube: Clube,
        val members: List<ClubeMember>,
        val kdr: Double
    )
}