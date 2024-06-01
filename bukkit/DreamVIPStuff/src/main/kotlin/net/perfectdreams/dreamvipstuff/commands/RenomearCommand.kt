package net.perfectdreams.dreamvipstuff.commands

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamvipstuff.DreamVIPStuff
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.jsoup.Jsoup
import java.util.*

object RenomearCommand : DSLCommandBase<DreamVIPStuff> {
    const val PRICE = 10_000
    val IS_RENAMED_BY_SEU_ZE_KEY = SparklyNamespacedBooleanKey("is_renamed_by_seu_ze")

    override fun command(plugin: DreamVIPStuff) = create(
        listOf("viprenomear", "viprename")
    ) {
        permission = "dreammini.viprename"

        executes {
            val itemInMainHand = player.inventory.itemInMainHand

            if (!player.hasPermission("dreammini.viprename")) {
                player.sendMessage("§cApenas VIP+ e VIP++ podem renomear itens com o Seu Zé!")
            } else {
                if (args.isEmpty()) {
                    player.sendMessage("§cSeu Zé precisa saber o nome do seu item! §6/viprenomear Novo Nome")
                } else {
                    if (itemInMainHand.type == Material.AIR) {
                        player.sendMessage("§cSeu Zé não gosta das suas piadinhas de tentar renomear o ar")
                        return@executes
                    }

                    val renamedBySeuZe = if (itemInMainHand.hasItemMeta()) itemInMainHand.itemMeta.persistentDataContainer.get(IS_RENAMED_BY_SEU_ZE_KEY) else false

                    val price = if (renamedBySeuZe) {
                        2000
                    } else {
                        PRICE
                    }

                    val totalPrice = price * itemInMainHand.amount

                    if (totalPrice > player.balance) {
                        player.sendMessage("§cCadê os $totalPrice sonecas? Seu Zé não trabalha de graça não parça!")
                    } else {
                        player.inventory.setItemInMainHand(
                            itemInMainHand.rename(args.joinToString(" ").colorize())
                                .meta<ItemMeta> {
                                    persistentDataContainer.set(IS_RENAMED_BY_SEU_ZE_KEY, true)
                                }
                        )
                        player.sendMessage("§aTá feito meu chapa! Cobrei $totalPrice sonecas de você para deixar o nome do seu item chaveeeexxx")
                        player.withdraw(totalPrice.toDouble(), TransactionContext(extra = "renomear um item com o comando `/renomear`"))
                    }
                }
            }
        }
    }
}