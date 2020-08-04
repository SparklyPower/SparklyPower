package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

object EventosCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("eventos")) {
        executes {
            sender.sendMessage("§8[ §bEventos do SparklyPower §8]".centralizeHeader())
            sender.sendMessage("§eVeja os jogadores que venceram mais eventos neste mês em §6/eventos top")
            sender.sendMessage("§7")

            val events = DreamCore.INSTANCE.dreamEventManager.getRunningEvents()
            if (events.isNotEmpty()) {
                events.forEach {
                    sender.sendMessage("§e${it.eventName}")
                }
            }

            val upcomingEvents = DreamCore.INSTANCE.dreamEventManager.getUpcomingEvents()

            val hasPlayers = upcomingEvents.filter { Bukkit.getOnlinePlayers().size >= it.requiredPlayers }
            val notEnoughPlayers = upcomingEvents.filter { it.requiredPlayers > Bukkit.getOnlinePlayers().size }
            for (ev in hasPlayers.sortedBy { (it.delayBetween + it.lastTime) - System.currentTimeMillis() }) {
                val diff = (ev.delayBetween + ev.lastTime) - System.currentTimeMillis()

                if (diff >= 0) {
                    var fancy = ""
                    if (diff >= (60000 * 60)) {
                        val minutes = ((diff / (1000 * 60)) % 60)
                        val hours = ((diff / (1000 * 60 * 60)) % 24)
                        fancy = String.format(
                            "%dh%dm",
                            hours,
                            minutes
                        )
                    } else if (diff >= 60000) {
                        fancy = String.format(
                            "%dm",
                            TimeUnit.MILLISECONDS.toMinutes(diff),
                            TimeUnit.MILLISECONDS.toSeconds(diff) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))
                        )
                    } else {
                        fancy = String.format(
                            "%ds",
                            TimeUnit.MILLISECONDS.toSeconds(diff) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))
                        )
                    }
                    sender.sendMessage("§d" + ev.eventName + " (" + fancy + ")")
                } else {
                    // It will start soon!!
                    sender.sendMessage("§d" + ev.eventName)
                }
            }
            for (ev in notEnoughPlayers.sortedBy { it.requiredPlayers }) {
                val requiredCount = ev.requiredPlayers - Bukkit.getOnlinePlayers().size
                val str = if (requiredCount == 1) "player" else "players"
                sender.sendMessage("§d" + ev.eventName + " (+$requiredCount $str)")
            }
        }
    }
}