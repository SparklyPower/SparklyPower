package net.perfectdreams.dreamcasamentos.dao

import net.perfectdreams.dreamcasamentos.tables.Adoptions
import net.perfectdreams.dreamcasamentos.tables.Marriages
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.*

class Adoption(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Adoption>(Adoptions)

    var adoptedBy by Marriage referencedOn Adoptions.adoptedBy
    var adotedAt by Adoptions.adotedAt

    var player by Adoptions.player
}