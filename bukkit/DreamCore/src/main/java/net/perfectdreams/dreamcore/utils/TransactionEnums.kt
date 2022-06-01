package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

enum class TransactionCurrency {
    MONEY,
    CASH
}

enum class TransactionType {
    PAYMENT,
    BUY_SHOP_ITEM,
    SELL_SHOP_ITEM,
    VOTE_REWARDS,
    BETTING,
    EVENTS,
    SECRET_BOXES,
    LSX,
    UNSPECIFIED
}

/**
 * @param payer User who paid [amount] in the transaction, only nullable if [receiver] is not null.
 * @param receiver User who received [amount] in the transaction, only nullable if [payer] is not null.
 * @param amount Must be a positive number
 * @param extra Useful information regarding a transaction. E.g.: in the case of a sign shop, it should be the
 * material of the item involved in the transaction.
 */
data class TransactionContext(
    var currency: TransactionCurrency = TransactionCurrency.MONEY,
    private val type: TransactionType = TransactionType.UNSPECIFIED,
    private val extra: String? = null,
    var payer: UUID? = null,
    var receiver: UUID? = null,
    var amount: Double = 0.0,
    private val time: Long = System.currentTimeMillis()
) {
    fun saveToDatabase() {
        if (payer == null && receiver == null)
            throw IllegalArgumentException("At least one of these fields [Payer, Receiver] must not be null.")

        if (amount <= 0.0)
            throw IllegalArgumentException("Amount of money must be a positive number.")

        DreamCore.INSTANCE.launchAsyncThread {
            transaction(Databases.databaseNetwork) {
                Transaction.new {
                    this.payer = this@TransactionContext.payer
                    this.receiver = this@TransactionContext.receiver
                    this.currency = this@TransactionContext.currency
                    this.amount = this@TransactionContext.amount
                    this.type = this@TransactionContext.type
                    this.time = this@TransactionContext.time
                    this.extra = this@TransactionContext.extra
                }
            }
        }
    }
}