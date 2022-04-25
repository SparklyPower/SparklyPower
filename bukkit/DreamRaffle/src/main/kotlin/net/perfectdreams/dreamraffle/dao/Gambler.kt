package net.perfectdreams.dreamraffle.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamraffle.tables.Gamblers
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class Gambler(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Gambler>(Gamblers) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) {
            find { Gamblers.id eq uuid }.firstOrNull()
        }
    }

    var victories by Gamblers.victories
    var wonSonecas by Gamblers.wonSonecas
    var spentSonecas by Gamblers.spentSonecas
    var wonCash by Gamblers.wonCash
    var spentCash by Gamblers.spentCash

    fun addVictory() = transaction(Databases.databaseNetwork) { victories++ }
    fun addWonSonecas(amount: Long) = transaction(Databases.databaseNetwork) { wonSonecas += amount }
    fun addSpentSonecas(amount: Long) = transaction(Databases.databaseNetwork) { spentSonecas += amount }
    fun addWonCash(amount: Long) = transaction(Databases.databaseNetwork) { wonCash += amount }
    fun addSpentCash(amount: Long) = transaction(Databases.databaseNetwork) { spentCash += amount }
}