package net.perfectdreams.dreamdiscordcommandrelayer.dao

import net.perfectdreams.dreamdiscordcommandrelayer.tables.Commands
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Command(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Command>(Commands)

    var player by Commands.player
    var world by Commands.world
    var alias by Commands.alias
    var args by Commands.args
    var time by Commands.time
    var x by Commands.x
    var y by Commands.y
    var z by Commands.z
}