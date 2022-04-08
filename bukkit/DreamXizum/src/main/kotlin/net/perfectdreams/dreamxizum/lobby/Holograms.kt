package net.perfectdreams.dreamxizum.lobby

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamcore.utils.TimeUtils.convertEpochMillisToAbbreviatedTime
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.Matchmaker.firstSeasonEnd
import net.perfectdreams.dreamxizum.battle.Matchmaker.firstSeasonStart
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.dao.Duelist
import net.perfectdreams.dreamxizum.tasks.UpdateLeaderboardTask
import org.bukkit.Bukkit
import java.awt.Color
import java.util.Date
import java.util.UUID
import kotlin.math.abs

object Holograms {
    private val npcModels = with (XizumConfig.models.npcs) { listOf(normal, ranked, sponsor1, sponsor2, builder) }
    lateinit var leaderboard: Hologram

    val hologramColors = getRainbowColors(14)

    val timeMessage: String get() =
        with (Date()) {
            val hasStarted = after(firstSeasonStart)
            val difference = abs(time - (if (hasStarted) firstSeasonEnd else firstSeasonStart).time)
            return (if (hasStarted) "Acaba em " else "Inicia em ") + convertEpochMillisToAbbreviatedTime(difference)
        }

    val leaderboardLines: List<String>
        get() = mutableListOf("${hologramColors[0]}⤖ Placar de Liderança ⬻").apply {
            val highestPoints = Duelist.leaderboard
            add("")
            for (index in 1..10) {
                val duelist = highestPoints.getOrNull(index-1)
                add("${hologramColors[index]}$index. " + (duelist?.let {
                    duelist.uuid.asUsername + " [${duelist.points}]"
                } ?: "vazio"))
            }
            add("")
            add("${hologramColors[11]}${Matchmaker.currentSeason}° temporada")
            add(hologramColors[12] + timeMessage)
        }

    fun spawnHolograms() {
        npcModels.forEach {
            val isBuilder = it == XizumConfig.models.npcs.builder
            val isLobbyNpc = with (XizumConfig.models.npcs) { it == normal || it == ranked }

            val location = it.coordinates.toBukkitLocation().apply { y += if (isBuilder) 2.385 else 1.6 }
            val text = if (isLobbyNpc) it.displayName.take(4) + "Clique para buscar uma" else it.displayName

            HologramsAPI.createHologram(DreamXizum.INSTANCE, location).appendTextLine(text)
        }
    }

    fun createLeaderboard() = with (XizumConfig.models.locations.leaderboard) {
        leaderboard = HologramsAPI.createHologram(DreamXizum.INSTANCE, toBukkitLocation()).apply {
            DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) {
                val lines = leaderboardLines
                switchContext(SynchronizationContext.SYNC)
                lines.forEach { appendTextLine(it) }
                appendTextLine("${hologramColors[13]}Atualizado recentemente")

                UpdateLeaderboardTask.startTask()
            }
        }
    }

    fun updateLeaderboard(lines: List<String>) = with (leaderboard) {
        lines.forEachIndexed { index, line ->
            if (index != 1 && index != 12) (getLine(index) as TextLine).text = line
        }
    }

    private fun getRainbowColors(quantity: Int): Array<String> = Array(quantity) {
        "§l" + ChatColor.of(Color.getHSBColor(it.toFloat() / quantity, .4F, .95F))
    }

    private val UUID.asUsername get() = Bukkit.getOfflinePlayer(this).name ?: toString()
}