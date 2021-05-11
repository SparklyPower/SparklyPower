package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import org.bukkit.Statistic
import org.jetbrains.exposed.sql.transactions.transaction

object OnlineCommand : DSLCommandBase<DreamChat> {
    override fun command(plugin: DreamChat) = create(listOf("online")) {
        executes {
            val oldestPlayers = plugin.oldestPlayers

            plugin.schedule(SynchronizationContext.ASYNC) {
                val userData = oldestPlayers.map {
                    transaction(Databases.databaseNetwork) {
                        User.findById(it.first)
                    }
                }

                switchContext(SynchronizationContext.SYNC)

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
                val numberOfDays = input / 86400
                val numberOfHours = input % 86400 / 3600
                val numberOfMinutes = input % 86400 % 3600 / 60

                sender.sendMessage("§eVocê tem §6$numberOfDays dias§e, §6$numberOfHours horas §ee §6$numberOfMinutes minutos§6 online!")
            }
        }
    }
}