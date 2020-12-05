package net.perfectdreams.dreamassinaturas.dao

import net.perfectdreams.dreamassinaturas.tables.Assinaturas
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.*

class Assinatura(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Assinatura>(Assinaturas)

    var signedBy by Assinaturas.signedBy
    var signedAt by Assinaturas.signedAt
    var worldName by Assinaturas.worldName
    var x by Assinaturas.x
    var y by Assinaturas.y
    var z by Assinaturas.z
    var yaw by Assinaturas.yaw
    var pitch by Assinaturas.pitch

    fun getLocation(): Location {
        return Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
    }

    fun setLocation(location: Location) {
        worldName = location.world.name
        x = location.x
        y = location.y
        z = location.z
        yaw = location.yaw
        pitch = location.pitch
    }
}