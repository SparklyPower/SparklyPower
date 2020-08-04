package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.tables.ClubeMembers
import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
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

object EventosTopMeuClubeCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("eventos top meuclube", "eventos top meuclan")) {
        executes {
            sender.sendMessage("§8[ §bTop Meu Clube de Evento do Mês §8]".centralizeHeader())

            async {
                val clube = ClubeAPI.getPlayerClube(player) ?: return@async

                val clubeVictoriesOnAverage = plugin.getClubeVictoriesOnThisMonth(clube)
                    .entries.sortedByDescending { it.value }

                for ((index, result) in clubeVictoriesOnAverage.withIndex()) {
                    val userInfo = transaction(Databases.databaseNetwork) { User.findById(result.key) }

                    player.sendMessage("§e${index + 1}. §b${userInfo?.username}§e com §a${result.value} vitórias")
                }
            }
        }
    }
}