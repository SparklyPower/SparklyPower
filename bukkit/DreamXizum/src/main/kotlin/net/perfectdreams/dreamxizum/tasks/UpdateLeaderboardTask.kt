package net.perfectdreams.dreamxizum.tasks

import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.dao.Duelist
import net.perfectdreams.dreamxizum.lobby.Holograms
import org.bukkit.Bukkit
import org.bukkit.Sound
import java.util.UUID

object UpdateLeaderboardTask {
    private var iterations = -1
    var hasAnnounced = Matchmaker.hasSeasonStarted
    var bestDuelist: UUID? = Duelist.leaderboard.firstOrNull()?.uuid

    fun startTask() =
        DreamXizum.INSTANCE.schedule {
            while (true) {
                if (!hasAnnounced && Matchmaker.hasSeasonStarted) announceFirstSeason() else iterations++

                val minutes = iterations % 5

                val lastLine = "${Holograms.hologramColors[13]}Atualizado " + if (minutes == 0) "recentemente"
                    else "há ${minutes.pluralize("minuto" to "minutos")} atrás"

                (Holograms.leaderboard.getLine(15) as TextLine).text = lastLine

                if (minutes == 0) {
                    switchContext(SynchronizationContext.ASYNC)

                    var newDuelist: UUID? = Duelist.leaderboard.firstOrNull()?.uuid
                    val lines = Holograms.leaderboardLines

                    switchContext(SynchronizationContext.SYNC)

                    Holograms.updateLeaderboard(lines)

                    if (newDuelist != bestDuelist && iterations > 0) {
                        val name = Bukkit.getOfflinePlayer(newDuelist!!).name!!

                        Bukkit.getOnlinePlayers().forEach {
                            it.sendMessage("${DreamXizum.PREFIX} ${highlight(name)} alcançou a primeira colocação no placar de liderança do Xizum competitivo.")
                            it.playSound(it.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 10F, 1F)
                        }

                        bestDuelist = newDuelist
                    }
                }

                waitFor(20L * 60)
            }
        }

    private fun announceFirstSeason() {
        iterations = 0
        hasAnnounced = true
        val sounds = setOf(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR)
        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage("${DreamXizum.PREFIX} A primeira temporada do modo competitivo do Xizum acaba de se iniciar. Digite ${highlight("/xizum")} para competir pelo pódio e conquistar recompensas grandiosas.")
            DreamXizum.INSTANCE.schedule {
                repeat (50) { _ ->
                    it.playSound(it.location, sounds.random(), 10F, 1F)
                    waitFor(3L)
                }
            }
        }
    }
}