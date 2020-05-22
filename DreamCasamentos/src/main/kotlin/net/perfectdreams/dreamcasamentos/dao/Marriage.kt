package net.perfectdreams.dreamcasamentos.dao

import net.perfectdreams.dreamcasamentos.tables.Marriages
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.*

class Marriage(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Marriage>(Marriages)

    var player1 by Marriages.player1
    var player2 by Marriages.player2

    var homeWorld by Marriages.homeWorld
    var homeX by Marriages.homeX
    var homeY by Marriages.homeY
    var homeZ by Marriages.homeZ

    fun getPartnerOf(player: Player): UUID {
        return getPartnerOf(player.uniqueId)
    }

    fun getPartnerOf(player: UUID): UUID {
        return if (this.player1 == player) {
            player2
        } else {
            player1
        }
    }

    fun getHomeLocation(): Location {
        return Location(Bukkit.getWorld(homeWorld!!), homeX!!, homeY!!, homeZ!!)
    }
}