package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class LojaOrderExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    inner class Options : CommandOptions() {
        val lojas = greedyString("lojas")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val lojas = args[options.lojas].replace(", ", " ").replace(",", " ").split(" ")

        if (lojas.isEmpty()) {
            context.sendLojaMessage {
                color(NamedTextColor.RED)
                append("Você precisa enviar uma lista de lojas!")
            }
        }

        val player = context.requirePlayer()

        m.launchAsyncThread {
            // Validate if all shops exist
            val result = transaction(Databases.databaseNetwork) {
                val queriedShops = Shop.find {
                    Shops.owner eq player.uniqueId and (Shops.shopName inList lojas)
                }.toList()

                val availableShopNames = queriedShops.map { it.shopName }

                val shopsPresentOnTheListThatDoesntExist = lojas.filter { it !in availableShopNames }

                if (shopsPresentOnTheListThatDoesntExist.isNotEmpty())
                    return@transaction ShopsPresentOnTheListThatDoesntExist(shopsPresentOnTheListThatDoesntExist)

                val shopsMissingFromTheList = availableShopNames.filter { it !in lojas }

                if (shopsMissingFromTheList.isNotEmpty())
                    return@transaction ShopsMissingFromTheList(shopsMissingFromTheList)

                // None are missing, update current shops!
                for (queriedShop in queriedShops) {
                    queriedShop.order = lojas.indexOf(queriedShop.shopName)
                }

                // Done!
                return@transaction Success
            }

            onMainThread {
                when (result) {
                    is ShopsPresentOnTheListThatDoesntExist -> {
                        context.sendLojaMessage {
                            color(NamedTextColor.RED)
                            if (result.missingShops.size == 1) {
                                append("A loja ${result.missingShops.first()} que você colocou na lista não existe!")
                            } else {
                                append("As lojas ${result.missingShops.first()} que você colocou na lista não existem!")
                            }
                        }
                    }
                    is ShopsMissingFromTheList -> {
                        context.sendLojaMessage {
                            color(NamedTextColor.RED)
                            if (result.missingShops.size == 1) {
                                append("A loja ${result.missingShops.first()} que você tem não está na lista!")
                            } else {
                                append("As lojas ${result.missingShops.first()} que você tem não estão na lista!")
                            }
                        }
                    }
                    Success -> {
                        context.sendLojaMessage {
                            color(NamedTextColor.GREEN)
                            append("Ordem atualizada com sucesso!")
                        }
                    }
                }
            }
        }
    }

    sealed class ShopOrderUpdateResponse

    class ShopsPresentOnTheListThatDoesntExist(val missingShops: List<String>) : ShopOrderUpdateResponse()
    class ShopsMissingFromTheList(val missingShops: List<String>) : ShopOrderUpdateResponse()
    data object Success : ShopOrderUpdateResponse()
}