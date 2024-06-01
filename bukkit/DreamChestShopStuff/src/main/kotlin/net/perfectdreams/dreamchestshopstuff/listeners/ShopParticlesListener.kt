package net.perfectdreams.dreamchestshopstuff.listeners

import com.Acrobot.ChestShop.Events.PreTransactionEvent
import net.perfectdreams.dreamchestshopstuff.DreamChestShopStuff
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ShopParticlesListener(val m: DreamChestShopStuff) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onTransaction(event: PreTransactionEvent) {
        if (event.transactionOutcome == PreTransactionEvent.TransactionOutcome.TRANSACTION_SUCCESFUL) {
            event.client.world.spawnParticle(Particle.HAPPY_VILLAGER, event.client.location.add(0.0, 0.5, 0.0), 3, 0.5, 0.5, 0.5)
        } else {
            event.client.world.spawnParticle(Particle.ANGRY_VILLAGER, event.client.location.add(0.0, 0.5, 0.0), 3, 0.5, 0.5, 0.5)
        }
    }
}