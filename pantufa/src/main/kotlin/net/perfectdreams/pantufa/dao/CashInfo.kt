package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.tables.Cashes
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class CashInfo(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CashInfo>(Cashes)

    var cash by Cashes.cash
}