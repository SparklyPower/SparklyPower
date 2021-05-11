package net.perfectdreams.dreamvipstuff.commands

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.colorize
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamvipstuff.DreamVIPStuff
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jsoup.Jsoup
import java.util.*

object RenomearCommand : DSLCommandBase<DreamVIPStuff> {
    const val PRICE = 10_000

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

                    val renamedBySeuZe = itemInMainHand.getStoredMetadata("renamedBySeuZe")?.toBoolean() ?: false

                    val price = if (renamedBySeuZe) {
                        2000
                    } else {
                        PRICE
                    }

                    val totalPrice = price * itemInMainHand.amount

                    if (totalPrice > player.balance) {
                        player.sendMessage("§cCadê os $totalPrice sonhos? Seu Zé não trabalha de graça não parça!")
                    } else {
                        player.inventory.setItemInMainHand(itemInMainHand.rename(args.joinToString(" ").colorize()).storeMetadata("renamedBySeuZe", "true"))
                        player.sendMessage("§aTá feito meu chapa! Cobrei $totalPrice sonhos de você para deixar o nome do seu item chaveeeexxx")
                        player.balance -= totalPrice
                    }
                }
            }
        }
    }
}