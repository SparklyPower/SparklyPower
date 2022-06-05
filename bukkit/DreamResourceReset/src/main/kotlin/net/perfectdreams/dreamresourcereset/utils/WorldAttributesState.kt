package net.perfectdreams.dreamresourcereset.utils

import org.bukkit.Server
import org.bukkit.World

sealed class WorldAttributesState(private val server: Server, val worldName: String) {
    val _world: World?
        get() = server.getWorld(worldName)

    val world: World
        get() = _world!!

    abstract fun canYouLoseItems(): Boolean

    class ResourcesWorldAttributesState(server: Server) : WorldAttributesState(server, "Resources") {
        override fun canYouLoseItems() = world.time !in 0 until 13_000 // https://minecraft.fandom.com/wiki/Daylight_cycle
    }

    class NetherWorldAttributesState(server: Server) : WorldAttributesState(server, "Nether") {
        override fun canYouLoseItems() = true
    }

    class TheEndWorldAttributesState(server: Server) : WorldAttributesState(server, "TheEndSlk") {
        override fun canYouLoseItems() = true
    }
}