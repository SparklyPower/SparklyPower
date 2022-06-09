package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.isUnsafe
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.tables.Shops
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SetLojaExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    companion object : SparklyCommandExecutorDeclaration(SetLojaExecutor::class) {
        object Options : CommandOptions() {
            val shopName = optionalWord("shop_name")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val shopName = args[Options.shopName]?.lowercase() ?: "loja"

        val location = player.location
        if (location.isUnsafe) {
            context.sendLojaMessage {
                color(NamedTextColor.RED)
                content("A sua localização atual é insegura! Vá para um lugar mais seguro antes de marcar a sua loja!")
            }
            return
        }

        var createdNew = false
        var valid = true

        val shopCountForPlayer = getMaxAllowedShops(player)

        m.launchAsyncThread {
            transaction(Databases.databaseNetwork) {
                val shop = Shop.find {
                    (Shops.owner eq player.uniqueId) and (Shops.shopName eq shopName)
                }.firstOrNull()

                val isNew = shop == null

                val shopCount = transaction(Databases.databaseNetwork) {
                    Shop.find { (Shops.owner eq player.uniqueId) }.count()
                }

                if (isNew) {
                    if (shopCount + 1 > shopCountForPlayer) {
                        context.sendMessage {
                            append(DreamLoja.PREFIX)
                            color(NamedTextColor.RED)

                            append("Você já tem muitas lojas! Delete algumas usando ")
                            appendCommand("/loja delete")
                            append("!")
                        }

                        valid = false
                        return@transaction
                    }
                    createdNew = true
                    Shop.new {
                        this.owner = player.uniqueId
                        this.shopName = shopName
                        setLocation(location)
                    }
                } else {
                    shop!!.setLocation(location)
                }
            }

            onMainThread {
                if (!valid)
                    return@onMainThread

                if (shopName == "loja") {
                    if (createdNew) {
                        context.sendLojaMessage {
                            color(NamedTextColor.GREEN)

                            append("Sua loja foi criada com sucesso! Outros jogadores podem ir até ela utilizando ")
                            appendCommand("/loja ${player.name}")
                            append("!")
                        }
                    } else {
                        context.sendLojaMessage {
                            color(NamedTextColor.GREEN)

                            append("Sua loja foi atualizada com sucesso! Outros jogadores podem ir até ela utilizando ")
                            appendCommand("/loja ${player.name}")
                            append("!")
                        }
                    }
                } else {
                    if (createdNew) {
                        context.sendLojaMessage {
                            color(NamedTextColor.GREEN)

                            append("Sua loja foi criada com sucesso! Outros jogadores podem ir até ela utilizando ")
                            appendCommand("/loja ${player.name} $shopName")
                            append("!")
                        }
                    } else {
                        context.sendLojaMessage {
                            append(DreamLoja.PREFIX)
                            color(NamedTextColor.GREEN)

                            append("Sua loja foi atualizada com sucesso! Outros jogadores podem ir até ela utilizando ")
                            appendCommand("/loja ${player.name} $shopName")
                            append("!")
                        }
                    }
                }

                if (shopCountForPlayer != 1) {
                    context.sendLojaMessage {
                        append(DreamLoja.PREFIX)
                        color(NamedTextColor.YELLOW)

                        append("Sabia que é possível alterar o ícone da sua loja na ")
                        appendCommand("/loja ${player.name}")
                        append("? Use ")
                        appendCommand("/loja icon $shopName")
                        append(" com o item na mão!")
                    }
                }
            }
        }
    }

    /**
     * Gets the max allowed homes for the [player]
     */
    fun getMaxAllowedShops(player: Player): Int {
        return when {
            player.hasPermission("dreamloja.lojaplusplusplus") -> 7
            player.hasPermission("dreamloja.lojaplusplus") -> 5
            player.hasPermission("dreamloja.lojaplus") -> 3
            else -> 1
        }
    }
}