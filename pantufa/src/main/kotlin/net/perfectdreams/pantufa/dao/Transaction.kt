package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.interactions.components.utils.TransactionCurrency
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Transactions
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class Transaction(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Transaction>(Transactions) {
        fun fetchTransactions(payer: UUID?, receiver: UUID?, currency: TransactionCurrency?) =
            transaction(Databases.sparklyPower) {
                find {
                    (payer?.let { Transactions.payer eq it } ?: Op.TRUE) and
                    (receiver?.let { Transactions.receiver eq it } ?: Op.TRUE) and
                    (currency?.let { Transactions.currency eq it } ?: Op.TRUE)
                }.orderBy(Transactions.time to SortOrder.DESC)
            }

        fun fetchTransactionsFromSingleUser(user: UUID, currency: TransactionCurrency?) =
            transaction(Databases.sparklyPower) {
                find {
                    (Transactions.payer eq user) or
                    (Transactions.receiver eq user) and
                    (currency?.let { Transactions.currency eq it } ?: Op.TRUE)
                }.orderBy(Transactions.time to SortOrder.DESC)
            }
    }

    var payer by Transactions.payer
    var receiver by Transactions.receiver
    var currency by Transactions.currency
    var type by Transactions.type
    var time by Transactions.time
    var amount by Transactions.amount
    var extra by Transactions.extra
}