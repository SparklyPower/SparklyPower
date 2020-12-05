package net.perfectdreams.dreamcash.dao

import net.perfectdreams.dreamcash.tables.Cashes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class CashInfo(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CashInfo>(Cashes)

    var cash by Cashes.cash
}