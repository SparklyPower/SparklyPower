package net.perfectdreams.dreamquickharvest.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DateUtils
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.suggestCommandOnClick
import net.perfectdreams.dreamcore.utils.adventure.textComponent
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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DreamQuickHarvestPlayerUpgradesListExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("America/Sao_Paulo")) // Required to format a Instant
    }

    inner class Options : CommandOptions() {
        val playerName = word("player_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val playerToBeChecked = args[options.playerName]

        m.launchAsyncThread {
            val userInfo = DreamUtils.retrieveUserInfo(playerToBeChecked)

            if (userInfo == null) {
                onMainThread {
                    context.sendMessage {
                        color(NamedTextColor.RED)

                        content("Player $playerToBeChecked não existe!")
                    }
                }
                return@launchAsyncThread
            }

            val upgrades = transaction(Databases.databaseNetwork) {
                PlayerQuickHarvestUpgrades.select {
                    PlayerQuickHarvestUpgrades.playerId eq userInfo.id.value and (PlayerQuickHarvestUpgrades.expiresAt greaterEq Instant.now())
                }.orderBy(PlayerQuickHarvestUpgrades.id, SortOrder.ASC)
                    .toList()
            }

            onMainThread {
                context.sendMessage {
                    color(NamedTextColor.AQUA)

                    content("Upgrades de ${userInfo.username}")
                }

                for (upgrade in upgrades) {
                    context.sendMessage {
                        color(NamedTextColor.YELLOW)

                        append(
                            textComponent {
                                color(NamedTextColor.GREEN)
                                content("#${upgrade[PlayerQuickHarvestUpgrades.id]}")
                            }
                        )

                        append(": Upgrade de ${upgrade[PlayerQuickHarvestUpgrades.energy]} energias")
                    }

                    context.sendMessage {
                        color(NamedTextColor.GRAY)

                        append(
                            textComponent {
                                color(NamedTextColor.DARK_GRAY)
                                content("• ")
                            }
                        )

                        append("Comprado em ${DATE_FORMATTER.format(upgrade[PlayerQuickHarvestUpgrades.boughtAt])}")
                    }

                    context.sendMessage {
                        color(NamedTextColor.GRAY)

                        append(
                            textComponent {
                                color(NamedTextColor.DARK_GRAY)
                                content("• ")
                            }
                        )

                        append("Expira em ${DATE_FORMATTER.format(upgrade[PlayerQuickHarvestUpgrades.expiresAt])}")
                    }

                    context.sendMessage {
                        color(NamedTextColor.RED)

                        content("  [Deletar]")

                        suggestCommandOnClick("/dreamquickharvest upgrades delete ${upgrade[PlayerQuickHarvestUpgrades.id]}")
                    }
                }
            }
        }
    }
}