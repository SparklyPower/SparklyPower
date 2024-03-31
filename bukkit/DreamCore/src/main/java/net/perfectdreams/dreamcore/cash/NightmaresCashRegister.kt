package net.perfectdreams.dreamcore.cash

import net.perfectdreams.dreamcore.utils.TransactionContext
import org.bukkit.entity.Player
import java.util.*

// Workaround due to cyclic dependencies in DreamCash -> DreamChat -> DreamLoja -> DreamCash
interface NightmaresCashRegister {
    companion object {
        lateinit var INSTANCE: NightmaresCashRegister // Should be initialized when DreamCash starts up
    }

    fun giveCash(player: Player, quantity: Long, transactionContext: TransactionContext)

    fun giveCash(uniqueId: UUID, quantity: Long, transactionContext: TransactionContext)

    fun takeCash(player: Player, quantity: Long, transactionContext: TransactionContext)

    fun takeCash(uniqueId: UUID, quantity: Long, transactionContext: TransactionContext)

    fun setCash(player: Player, quantity: Long) = setCash(player.uniqueId, quantity)

    fun setCash(uniqueId: UUID, quantity: Long)

    fun getCash(player: Player): Long

    fun getCash(uniqueId: UUID): Long
}