package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.tables.ClubeMembers
import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object EventosTopClubesCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("eventos top clubes", "eventos top clan", "eventos top clans", "eventos top clube")) {
        executes {
            sender.sendMessage("§8[ §bTop Clubes de Evento do Mês §8]".centralizeHeader())
            sender.sendMessage("§eVocê precisa ter 5 membros no seu clube para estar no ranking!")
            sender.sendMessage("§eVeja os membros do seu clube que ganharam mais eventos em §6/eventos top meuclube§e!")
            sender.sendMessage("§7")
            async {
                val clubeVictoriesOnAverage = plugin.getTopClubeVictoriesOnAverageOnThisMonth()

                for ((index, result) in clubeVictoriesOnAverage.take(10).withIndex()) {
                    val clube = result.first

                    player.sendMessage("§e${index + 1}. §7${clube.shortName} §7(${clube.name}§7)§e com uma média de §a${String.format("%.2f", result.second)} vitórias")
                    if (index == 0) {
                        player.sendMessage("§8> §c§lCADA MEMBRO GANHA UM PESADELOS A CADA HORA ONLINE!")
                    }
                }
            }
        }
    }
}