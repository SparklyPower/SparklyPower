package net.perfectdreams.dreamchat.commands

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.sendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.Statistic
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId

object OnlineCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("online")) {
        executes {
            val oldestPlayers = plugin.oldestPlayers
            val player = player

            plugin.launchAsyncThread {
                val userData = oldestPlayers.map {
                    transaction(Databases.databaseNetwork) {
                        User.findById(it.first)
                    }
                }

                val timestamp = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
                    .minusMonths(1L)

                val survivalTrackedOnlineHoursDuration = Databases.cooked.transaction {
                    it.query("SELECT EXTRACT(epoch FROM SUM(logged_out - logged_in)) AS count FROM survival_trackedonlinehours WHERE player = ? AND logged_out >= ?") {
                        setObject(1, player.uniqueId)
                        setObject(2, timestamp)
                    }.firstOrNull()?.let { Duration.ofSeconds(it.getLong("count")) }
                } ?: Duration.ZERO

                onMainThread {
                    plugin.oldestPlayers.forEachIndexed { index, pair ->
                        val userName = userData.firstOrNull { it?.id?.value == pair.first }?.username ?: pair.first.toString()

                        val input = pair.second / 20
                        val numberOfDays = input / 86400
                        val numberOfHours = input % 86400 / 3600
                        val numberOfMinutes = input % 86400 % 3600 / 60

                        sender.sendMessage("§e${index + 1}. §b${userName} §ecom §6$numberOfDays dias§e, §6$numberOfHours horas §ee §6$numberOfMinutes minutos§6 online!")
                    }

                    sender.sendMessage("§e")
                    val input = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20

                    sender.sendTextComponent {
                        color(NamedTextColor.YELLOW)
                        append("Você tem ")
                        append(fancyDateFormat(input.toLong()))
                        append(" online!")
                    }

                    sender.sendTextComponent {
                        color(NamedTextColor.YELLOW)
                        append("Você tem ")
                        append(fancyDateFormat(survivalTrackedOnlineHoursDuration.seconds))
                        append(" online nos últimos 30 dias!")
                    }
                }
            }
        }
    }

    private fun fancyDateFormat(inputInSeconds: Long): TextComponent {
        val numberOfDays = inputInSeconds / 86400
        val numberOfHours = inputInSeconds % 86400 / 3600
        val numberOfMinutes = inputInSeconds % 86400 % 3600 / 60

        return textComponent {
            append("$numberOfDays dias") {
                color(NamedTextColor.GOLD)
            }
            append(", ")
            append("$numberOfHours horas") {
                color(NamedTextColor.GOLD)
            }
            append(", ")
            append("$numberOfMinutes minutos") {
                color(NamedTextColor.GOLD)
            }
        }
    }
}