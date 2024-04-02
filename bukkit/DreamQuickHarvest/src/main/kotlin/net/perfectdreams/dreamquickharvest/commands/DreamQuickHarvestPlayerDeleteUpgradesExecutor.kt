package net.perfectdreams.dreamquickharvest.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DateUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import net.perfectdreams.dreamquickharvest.tables.PlayerQuickHarvestUpgrades
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class DreamQuickHarvestPlayerDeleteUpgradesExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val upgradeId = integer("upgrade_id")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val upgradeId = args[options.upgradeId].toLong()

        m.launchAsyncThread {
            val deletedUpgrades = transaction(Databases.databaseNetwork) {
                PlayerQuickHarvestUpgrades.deleteWhere {
                    PlayerQuickHarvestUpgrades.id eq upgradeId
                }
            }

            onMainThread {
                if (deletedUpgrades == 0) {
                    context.sendMessage {
                        color(NamedTextColor.RED)

                        content("Upgrade #${upgradeId} não existe! Será que ele já foi deletado?")
                    }
                } else {
                    context.sendMessage {
                        color(NamedTextColor.GREEN)

                        content("Upgrade #${upgradeId} deletado!")
                    }
                }
            }
        }
    }
}