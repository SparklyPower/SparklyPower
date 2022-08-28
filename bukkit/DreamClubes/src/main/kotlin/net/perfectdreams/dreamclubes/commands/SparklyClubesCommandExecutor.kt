package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.entity.Player

abstract class SparklyClubesCommandExecutor(val m: DreamClubes) : SparklyCommandExecutor() {
    fun withPlayerClube(player: Player, action: suspend (Clube, ClubeMember) -> (Unit)) {
        m.launchAsyncThread {
            val clube = ClubeAPI.getPlayerClube(player)
            val clubeMember = clube?.retrieveMember(player)

            onMainThread {
                if (clube != null && clubeMember != null)
                    action.invoke(clube, clubeMember)
                else
                    player.sendMessage("§cVocê não está em um clube!")
            }
        }
    }
}