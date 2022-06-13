package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SetLojaIconExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    companion object : SparklyCommandExecutorDeclaration(SetLojaIconExecutor::class) {
        object Options : CommandOptions() {
            val shopName = optionalGreedyString("shop_name")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val shopName = m.parseLojaName(args[Options.shopName])

        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type.isAir) {
            context.sendLojaMessage {
                color(NamedTextColor.RED)

                append("Segure o item que você deseja que fique no ícone da sua ")
                appendCommand("/loja")
                append("!")
            }
            return
        }

        m.launchAsyncThread {
            val shop = transaction(Databases.databaseNetwork) {
                Shop.find {
                    (Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
                }.firstOrNull()
            }

            if (shop == null) {
                context.sendLojaMessage {
                    color(NamedTextColor.RED)
                    content("Loja desconhecida!")
                }
                return@launchAsyncThread
            }

            transaction(Databases.databaseNetwork) {
                shop.iconItemStack = itemInHand.clone()
                    .apply {
                        if (!this.itemMeta.hasDisplayName())
                            this.itemMeta = this.itemMeta.apply {
                                this.setDisplayName("§a${shop.shopName}")
                            }
                    }
                    .toBase64()
            }

            context.sendLojaMessage {
                color(NamedTextColor.GREEN)

                append("Ícone alterado com sucesso! Veja o novo look da sua loja em ")
                appendCommand("/loja")
                append("!")
            }
        }
    }
}