package net.perfectdreams.dreamxizum.commands.subcommands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.stripColors
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.elo.Leagues
import net.perfectdreams.dreamxizum.dao.Duelist
import net.perfectdreams.dreamxizum.lobby.Holograms

class XizumRankExecutor(val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumRankExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        if (!Matchmaker.hasSeasonStarted) context.fail("§cA temporada não começou ainda.")

        plugin.schedule(SynchronizationContext.ASYNC) {
            val points = Duelist.fetch(player.uniqueId).points

            val league = Leagues.getDivision(points).league.lowercase()
            val leaderboard = Holograms.leaderboardLines.subList(2, 12)

            player.sendMessage("${DreamXizum.PREFIX} Você tem $points pontos. " +
                if (points < 400) "Você não pertence à nenhuma divisão atualmente." else "Sua divisão atual é $league.")

            leaderboard.forEach { player.sendMessage(DreamXizum.COLOR + it.stripColors()) }
        }
    }
}