package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TextUtils
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.jetbrains.exposed.sql.transactions.transaction

class LojaListExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    companion object : SparklyCommandExecutorDeclaration(LojaListExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val userShops = transaction(Databases.databaseNetwork) {
                Shop.find {
                    Shops.owner eq player.uniqueId
                }.toList()
            }

            onMainThread {
                if (userShops.isEmpty()) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append("Você não tem nenhuma loja marcada! Marque uma usando ")
                        appendCommand("/loja manage set")
                        append("!")
                    }
                    return@onMainThread
                }

                player.sendMessage("§8[ §9Suas Lojas §8]".centralizeHeader())

                for (loja in userShops) {
                    context.sendMessage {
                        color(NamedTextColor.GRAY)
                        appendCommand("/loja ${player.name} ${loja.shopName}")
                        append(" (${loja.worldName}, ${TextUtils.ROUND_TO_2_DECIMAL.format(loja.x)}, ${TextUtils.ROUND_TO_2_DECIMAL.format(loja.y)}, ${TextUtils.ROUND_TO_2_DECIMAL.format(loja.z)})")
                    }
                }

                player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
            }
        }
    }
}