package net.perfectdreams.dreamcore.tables

import net.perfectdreams.dreamcore.utils.TransactionCurrency
import net.perfectdreams.dreamcore.utils.TransactionType
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