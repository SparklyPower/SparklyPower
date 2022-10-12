package net.perfectdreams.dreamquickharvest.commands

import kotlinx.datetime.*
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import net.perfectdreams.dreamquickharvest.tables.PlayerQuickHarvestUpgrades
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ColheitaUpgradeExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    
    private val upgrades = listOf(
        EnergyUpgrade(
            Material.COAL_BLOCK,
            1_000,
            100
        ),
        EnergyUpgrade(
            Material.IRON_BLOCK,
            2_100,
            200
        ),
        EnergyUpgrade(
            Material.GOLD_BLOCK,
            3_200,
            300
        ),
        EnergyUpgrade(
            Material.DIAMOND_BLOCK,
            4_300,
            400
        ),
        EnergyUpgrade(
            Material.EMERALD_BLOCK,
            5_400,
            500
        ),
        EnergyUpgrade(
            Material.NETHERITE_BLOCK,
            6_500,
            600
        )
    )

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val menu = createMenu(9, "Upgrade") {
            // 1000 - 100
            // 2100 - 200
            // 3200 - 300
            // 4300 - 400
            // 5400 - 500
            // 6500 - 600
            for ((index, upgrade) in upgrades.withIndex()) {
                slot(index) {
                    item = ItemStack(upgrade.material)
                        .rename("§e+${upgrade.energy} energia§a por ${upgrade.pesadelos} pesadelos")
                        .lore(
                            "§7Aumenta a sua energia, o upgrade dura um mês!"
                        )

                    onClick {
                        it.closeInventory()
                        m.launchAsyncThread {
                            val cash = Cash.getCash(player)

                            if (upgrade.pesadelos > cash) {
                                onMainThread {
                                    player.sendMessage("§cVocê não tem pesadelos suficientes para este upgrade!")
                                }
                                return@launchAsyncThread
                            }

                            val now = Clock.System.now()
                            transaction(Databases.databaseNetwork) {
                                PlayerQuickHarvestUpgrades.insert {
                                    it[PlayerQuickHarvestUpgrades.playerId] = player.uniqueId
                                    it[PlayerQuickHarvestUpgrades.energy] = upgrade.energy
                                    it[PlayerQuickHarvestUpgrades.boughtAt] = now.toJavaInstant()
                                    it[PlayerQuickHarvestUpgrades.expiresAt] = now.plus(
                                        1,
                                        DateTimeUnit.MONTH,
                                        TimeZone.of("America/Sao_Paulo")
                                    ).toJavaInstant()
                                }
                            }

                            Cash.takeCash(player, upgrade.pesadelos, TransactionContext(extra = "upgrade de ${upgrade.energy} energia no sistema de colheita rápida"))

                            onMainThread {
                                player.sendMessage("§aUpgrade aplicado!")
                            }
                        }
                    }
                }
            }
        }

        menu.sendTo(player)
    }

    private data class EnergyUpgrade(
        val material: Material,
        val energy: Int,
        val pesadelos: Long
    )
}