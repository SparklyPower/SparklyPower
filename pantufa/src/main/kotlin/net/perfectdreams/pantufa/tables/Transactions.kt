package net.perfectdreams.pantufa.tables

import net.perfectdreams.pantufa.interactions.components.utils.TransactionCurrency
import net.perfectdreams.pantufa.interactions.components.utils.TransactionType
import org.jetbrains.exposed.dao.id.LongIdTable

object Transactions : LongIdTable() {
    val payer = uuid("payer").index().nullable()
    val receiver = uuid("receiver").index().nullable()
    val currency = enumeration("currency", TransactionCurrency::class).index()
    val type = enumeration("type", TransactionType::class)
    val time = long("time")
    val amount = double("amount")
    val extra = text("extra").nullable()
}