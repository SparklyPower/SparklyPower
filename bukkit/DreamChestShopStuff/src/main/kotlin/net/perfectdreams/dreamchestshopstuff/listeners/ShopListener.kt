package net.perfectdreams.dreamchestshopstuff.listeners

import club.minnced.discord.webhook.WebhookClient
import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class ShopListener(val webhookClient: WebhookClient) : Listener {
    companion object {
        const val VIP_PLUS_PLUS_DISCOUNT = 0.15
        const val VIP_PLUS_DISCOUNT = 0.10
        const val VIP_DISCOUNT = 0.05
        val TRACKED_MATERIALS = setOf(
            Material.DIAMOND,
            Material.DIAMOND_BLOCK,
            Material.NETHERITE_INGOT,
            Material.NETHERITE_BLOCK
        )
    }

    val trackedSoldItemsByPlayer = mutableMapOf<UUID, MutableList<BoughtEntry>>()

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
        if (e.transactionType != TransactionEvent.TransactionType.SELL)
            return

        val triggeredMaterials = mutableSetOf<Material>()
        for (stock in e.stock) {
            if (stock.type in TRACKED_MATERIALS) {
                val trackedList = trackedSoldItemsByPlayer.getOrPut(e.client.uniqueId) { mutableListOf() }
                trackedList.add(BoughtEntry(stock.type, stock.amount, Instant.now()))
                triggeredMaterials.add(stock.type)
            }
        }

        // Should we notify?
        for (material in triggeredMaterials) {
            val trackedList = trackedSoldItemsByPlayer[e.client.uniqueId]
            if (trackedList != null) {
                val quantitySold = trackedList.filter { it.material == material }.sumOf { it.quantity }

                if (quantitySold >= (2304 * 5)) {
                    // Notify!
                    webhookClient.send("<@&332650495522897920> **`${e.client.name}`** (`${e.client.uniqueId}`) vendeu muitos `${material}` nos últimos 15 minutos! (Quantidade: $quantitySold)")
                }
            }
        }

        // Clean up!
        trackedSoldItemsByPlayer[e.client.uniqueId]?.removeIf { Instant.now() >= it.boughtAt.plus(Duration.ofMinutes(15)) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onLeave(e: PlayerQuitEvent) {
        // Clean up!
        trackedSoldItemsByPlayer.remove(e.player.uniqueId)
    }

    data class BoughtEntry(
        val material: Material,
        val quantity: Int,
        val boughtAt: Instant
    )
}