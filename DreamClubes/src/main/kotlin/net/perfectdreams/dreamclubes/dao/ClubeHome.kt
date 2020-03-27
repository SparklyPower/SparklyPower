package net.perfectdreams.dreamclubes.dao

import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.tables.Clubes.index
import net.perfectdreams.dreamclubes.tables.Clubes.nullable
import net.perfectdreams.dreamclubes.tables.ClubesHomes
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.*

class ClubeHome(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ClubeHome>(ClubesHomes)

    var worldName by ClubesHomes.worldName
    var x by ClubesHomes.x
    var y by ClubesHomes.y
    var z by ClubesHomes.z
    var yaw by ClubesHomes.yaw
    var pitch by ClubesHomes.pitch
}