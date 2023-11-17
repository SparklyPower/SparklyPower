package net.perfectdreams.dreamcore.utils.npc.user

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import org.bukkit.Bukkit
import org.bukkit.Location

@Serializable
data class UserCreatedNPCData(
    val id: Int,
    var name: String,
    var location: LocationReference,
    var lookClose: Boolean,
    var skin: CustomSkin?
) {
    @Serializable
    data class CustomSkin(
        var textures: SkinTexture,
        var skinTextureSource: SkinTextureSource,
        var autoRefreshSkin: Boolean,
        var queriedAt: Instant
    ) {
        @Serializable
        sealed class SkinTextureSource {
            @Serializable
            class MojangTextureSource(
                val uniqueId: String
            ) : SkinTextureSource()

            @Serializable
            class SparklyTextureSource(
                val uniqueId: String
            ) : SkinTextureSource()

            @Serializable
            data object MineSkinTextureSource : SkinTextureSource()
        }
    }

    @Serializable
    data class LocationReference(
        val worldName: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float
    ) {
        companion object {
            fun fromBukkit(location: Location) = LocationReference(location.world.name, location.x, location.y, location.z, location.yaw, location.pitch)
        }

        fun toBukkit() = Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
    }
}