package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Commands
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class Command(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Command>(Commands) {
        fun fetchCommands(player: String?, world: String?, alias: String?, args: String?) =
            transaction(Databases.sparklyPower) {
                find {
                    (player?.let { Commands.player eq it } ?: Op.TRUE) and
                    (world?.let { Commands.world eq it } ?: Op.TRUE) and
                    (alias?.let { Commands.alias eq it } ?: Op.TRUE) and
                    (args?.let { Commands.args regexp it } ?: Op.TRUE)
                }.orderBy(Commands.time to SortOrder.DESC)
            }
    }

    var player by Commands.player
    var world by Commands.world
    var alias by Commands.alias
    var args by Commands.args
    var time by Commands.time
    var x by Commands.x
    var y by Commands.y
    var z by Commands.z
}