package net.perfectdreams.dreamcore.dao

import net.perfectdreams.dreamcore.tables.Transactions
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Transaction(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Transaction>(Transactions)

    var payer by Transactions.payer
    var receiver by Transactions.receiver
    var currency by Transactions.currency
    var type by Transactions.type
    var time by Transactions.time
    var amount by Transactions.amount
    var extra by Transactions.extra
}