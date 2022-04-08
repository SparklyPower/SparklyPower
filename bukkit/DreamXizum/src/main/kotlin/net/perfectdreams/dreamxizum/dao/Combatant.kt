package net.perfectdreams.dreamxizum.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamxizum.tables.Combatants
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Combatant(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Combatant>(Combatants) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) {
            find { Combatants.id eq uuid }.firstOrNull() ?: new(uuid) {}
        }
    }

    var victories by Combatants.victories
    var defeats by Combatants.defeats
    var banned by Combatants.banned
    var tos by Combatants.tos

    fun addVictory() = transaction(Databases.databaseNetwork) { victories++ }
    fun addDefeat() = transaction(Databases.databaseNetwork) { defeats++ }
    fun acceptTOS() = transaction(Databases.databaseNetwork) { tos = true }
    fun ban() = transaction(Databases.databaseNetwork) {
        Duelist.fetch(this@Combatant.id.value).clearStats()
        banned = true
    }
}