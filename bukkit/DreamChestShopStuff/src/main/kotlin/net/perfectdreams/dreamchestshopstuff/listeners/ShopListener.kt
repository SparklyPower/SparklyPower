package net.perfectdreams.dreamchestshopstuff.listeners

import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ShopListener : Listener {
    companion object {
        const val VIP_PLUS_PLUS_DISCOUNT = 0.15
        const val VIP_PLUS_DISCOUNT = 0.10
        const val VIP_DISCOUNT = 0.05
    }

    // low para evitar que possam comprar em placas erradas
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onClick(e: PreTransactionEvent) {
        // Bukkit.broadcastMessage("Wow, transação em alguma loja (teste)")
        if (!e.ownerAccount.name.equals("SparklyShop", true)) // Apenas para Admin Shops!
            return

        if (e.client.world.name != "world") // Apenas no mundo normal
            return

        // Bukkit.broadcastMessage("Wow estão comprando na loja do spawn!")
        if (e.price == 0.0 || e.transactionOutcome != PreTransactionEvent.TransactionOutcome.TRANSACTION_SUCCESFUL) // Apenas se não é de graça
            return

        val zoneId = ZoneId.of("America/Sao_Paulo")
        val isAfterChange = LocalDateTime.of(
            2020,
            10,
            30,
            0,
            0,
            0,
            0
        ).atZone(zoneId)
            .isBefore(
                Instant.now()
                    .atZone(zoneId)
            )

        if (e.transactionType == TransactionEvent.TransactionType.BUY) {
            if (isAfterChange) {
                if (e.client.hasPermission("group.vip"))
                    e.price *= 1 - VIP_PLUS_DISCOUNT
            } else {
                e.price *= when {
                    e.client.hasPermission("group.vip++") -> 1 - VIP_PLUS_PLUS_DISCOUNT
                    e.client.hasPermission("group.vip+") -> 1 - VIP_PLUS_DISCOUNT
                    e.client.hasPermission("group.vip") -> 1 - VIP_DISCOUNT
                    else -> 1.0
                }
            }
        }

        if (e.transactionType == TransactionEvent.TransactionType.SELL) {
            if (isAfterChange) {
                if (e.client.hasPermission("group.vip"))
                    e.price *= 1 + VIP_PLUS_DISCOUNT
            } else {
                e.price *= when {
                    e.client.hasPermission("group.vip++") -> 1 + VIP_PLUS_PLUS_DISCOUNT
                    e.client.hasPermission("group.vip+") -> 1 + VIP_PLUS_DISCOUNT
                    e.client.hasPermission("group.vip") -> 1 + VIP_DISCOUNT
                    else -> 1.0
                }
            }
        }
    }

    /* // low para evitar que possam comprar em placas erradas
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun doNotAllowBuy(e: PreTransactionEvent) {
        if (e.ownerAccount.name.equals("SparklyShop", true)) // Apenas para Admin Shops!
            return

        if (e.transactionType != TransactionEvent.TransactionType.SELL)
            return

        val itemType = e.stock.firstOrNull()?.type

        if (itemType == Material.DIAMOND) {
            val correctPrice = (e.exactPrice / e.stock.sumBy { it.amount }.toBigDecimal())
                .toDouble()

            if (621 > correctPrice) {
                e.isCancelled = true
                e.client.sendMessage("§cNão pode!")
            }
        }
    } */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun storeTransaction(e: TransactionEvent) {

    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTransaction(e: TransactionEvent) {
        // Original Message: > :credit_card: **`JuuuuaoSP`** comprou *1 Pedregulho* de **`SparklyShop`** por *0.4 Sonhos* em `world` `516`, `65`, `283`
    }
}