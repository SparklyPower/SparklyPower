package net.perfectdreams.dreamxizum.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamxizum.battle.Matchmaker.currentSeason
import net.perfectdreams.dreamxizum.tables.Duelists
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Duelist(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Duelist>(Duelists) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) {
            find { (Duelists.uuid eq uuid) and (Duelists.season eq currentSeason) }.firstOrNull()
                ?: new {
                    this.uuid = uuid
                    this.season = currentSeason
                }
        }

        val leaderboard get() = transaction(Databases.databaseNetwork) {
                find { Duelists.season eq currentSeason }
                    .orderBy(Duelists.points to SortOrder.DESC)
                    .limit(10)
                    .toList()
                }
    }

    var uuid by Duelists.uuid
    var season by Duelists.season
    var victories by Duelists.victories
    var defeats by Duelists.defeats
    var points by Duelists.points
    var highestScore by Duelists.highestScore

    fun addVictory() = transaction(Databases.databaseNetwork) {
        Combatant.fetch(uuid).addVictory()
        victories++
    }

    fun addDefeat() = transaction(Databases.databaseNetwork) {
        Combatant.fetch(uuid).addDefeat()
        defeats++
    }

    fun addPoints(amount: Int) = transaction(Databases.databaseNetwork) {
        points += amount
        if (points > highestScore) highestScore = points
    }

    fun clearStats() = transaction(Databases.databaseNetwork) {
        addPoints(-points)
        victories = 0
        defeats = 0
    }
}