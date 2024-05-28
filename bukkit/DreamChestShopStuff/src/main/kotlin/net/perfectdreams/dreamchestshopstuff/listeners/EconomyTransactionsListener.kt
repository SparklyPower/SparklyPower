package net.perfectdreams.dreamchestshopstuff.listeners

import com.Acrobot.ChestShop.Events.TransactionEvent
import net.perfectdreams.dreamchestshopstuff.DreamChestShopStuff
import net.perfectdreams.dreamcore.utils.TransactionContext
import net.perfectdreams.dreamcore.utils.TransactionType
import net.perfectdreams.dreamcore.utils.getLocalizedName
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class EconomyTransactionsListener(private val plugin: DreamChestShopStuff) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTransaction(event: TransactionEvent) = plugin.launchAsyncThread {
        val transactionMessage = with (event.stock) {
            "${toList().sumOf { it.amount }}x ${first().getLocalizedName()}"
        }

        val isBuying = event.transactionType == TransactionEvent.TransactionType.BUY

        val owner = event.ownerAccount.name.let { if (it == "SparklyShop") null else Bukkit.getOfflinePlayer(it).uniqueId }
        val client = event.client.uniqueId

        TransactionContext(
            payer = if (isBuying) client else owner,
            receiver = if (isBuying) owner else client,
            type = if (isBuying) TransactionType.BUY_SHOP_ITEM else TransactionType.SELL_SHOP_ITEM,
            amount = event.exactPrice.toDouble(),
            extra = transactionMessage
        ).saveToDatabase()
    }
}