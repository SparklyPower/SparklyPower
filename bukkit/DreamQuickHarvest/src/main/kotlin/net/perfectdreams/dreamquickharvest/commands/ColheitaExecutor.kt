package net.perfectdreams.dreamquickharvest.commands

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DateUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import net.perfectdreams.dreamquickharvest.tables.PlayerQuickHarvestUpgrades
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class ColheitaExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(ColheitaExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val now = Instant.now()

        m.launchMainThread {
            m.loadAndUpdateUserEnergy(player) { info ->
                context.sendMessage("§aAtualmente você tem §e${info.activeBlocks} energias§a de bloco de plantação que você pode quebrar rapidamente")
                context.sendMessage("§aNo total, você pode ter §e${DreamQuickHarvest.DEFAULT_BLOCKS} energias§a de colheita rápida")
                context.sendMessage("§aVocê ganha §e${m.howManyQuickHarvestBlocksThePlayerShouldEarn(player)} energias§a de colheita por segundo. Ao subir nível de herbalismo, a sua colheita rápida aumenta mais rápido (Até o Nível ${DreamQuickHarvest.HERBALISM_LEVEL_CAP})")
                context.sendMessage("§aCada bloco de plantação usam energias diferentes, sendo eles...")
                DreamQuickHarvest.BLOCK_ENERGY_COST.forEach {
                    context.sendMessage("§e${it.key}§a: ${it.value}")
                }
                context.sendMessage("§aVocê pode aumentar o seu limite de energia da colheita rápida com pesadelos! §e/colheita upgrade")
                context.sendMessage("§aUpgrades ativos:")

                onAsyncThread {
                    val upgrades = transaction(Databases.databaseNetwork) {
                        PlayerQuickHarvestUpgrades.select { PlayerQuickHarvestUpgrades.playerId eq player.uniqueId and (PlayerQuickHarvestUpgrades.expiresAt greater now) }
                            .toList()
                    }

                    onMainThread {
                        for (upgrade in upgrades) {
                            context.sendMessage("§6+§e${upgrade[PlayerQuickHarvestUpgrades.energy]} energias §8(§7${DateUtils.formatDateDiff(upgrade[PlayerQuickHarvestUpgrades.expiresAt].toEpochMilli())}§8)")
                        }
                    }
                }
            }
        }
    }
}