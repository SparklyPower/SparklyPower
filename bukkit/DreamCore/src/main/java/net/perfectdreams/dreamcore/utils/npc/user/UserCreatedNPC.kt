package net.perfectdreams.dreamcore.utils.npc.user

import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import org.bukkit.Location

class UserCreatedNPC(
    val data: UserCreatedNPCData,
    val sparklyNPC: SparklyNPC
) {
    var lookClose: Boolean
        get() = data.lookClose
        set(value) {
            data.lookClose = value
            sparklyNPC.lookClose = value
        }

    fun teleport(location: Location) {
        data.location = UserCreatedNPCData.LocationReference.fromBukkit(location)
        sparklyNPC.teleport(location)
    }

    fun setPlayerName(name: String) {
        data.name = name
        sparklyNPC.setPlayerName(name)
    }

    fun setTextures(customSkin: UserCreatedNPCData.CustomSkin?) {
        data.skin = customSkin
        sparklyNPC.setPlayerTextures(customSkin?.textures)
    }
}