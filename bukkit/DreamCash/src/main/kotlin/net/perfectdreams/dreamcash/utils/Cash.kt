package net.perfectdreams.dreamcash.utils

import net.perfectdreams.dreamcash.dao.CashInfo
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.util.*

object Cash {
    fun giveCash(player: Player, quantity: Long) = giveCash(player.uniqueId, quantity)

    fun giveCash(uniqueId: UUID, quantity: Long) {
        if (0 >= quantity)
            throw IllegalArgumentException("Quantity is less or equal to zero! quantity = $quantity")

        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            cashInfo.cash += quantity
        }
    }

    fun takeCash(player: Player, quantity: Long) = takeCash(player.uniqueId, quantity)

    fun takeCash(uniqueId: UUID, quantity: Long) {
        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            if (quantity > cashInfo.cash)
                throw IllegalArgumentException("Quantity is more than player has! quantity = $quantity cashInfo.cash = ${cashInfo.cash}")

            cashInfo.cash -= quantity
        }
    }

    fun setCash(player: Player, quantity: Long) = setCash(player.uniqueId, quantity)

    fun setCash(uniqueId: UUID, quantity: Long) {
        if (0 > quantity)
            throw IllegalArgumentException("New quantity is less than zero! quantity = $quantity")

        transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId) ?: CashInfo.new(uniqueId) {
                    this.cash = 0
                }
            }

            cashInfo.cash = quantity
        }
    }

    fun getCash(player: Player) = getCash(player.uniqueId)

    fun getCash(uniqueId: UUID): Long {
        return transaction(Databases.databaseNetwork) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(uniqueId)
            }

            cashInfo?.cash ?: 0
        }
    }
}