package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.tables.Shops
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteLojaExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    companion object : SparklyCommandExecutorDeclaration(DeleteLojaExecutor::class) {
        object Options : CommandOptions() {
            val shopName = optionalGreedyString("shop_name")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val shopName = m.parseLojaName(args[Options.shopName])

        m.launchAsyncThread {
            transaction(Databases.databaseNetwork) {
                Shops.deleteWhere {
                    (Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
                }
            }

            context.sendLojaMessage {
                color(NamedTextColor.GREEN)

                append("Sua loja foi deletada com sucesso!")
            }
        }
    }
}