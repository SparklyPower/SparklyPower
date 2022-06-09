package net.perfectdreams.dreamresourcereset.utils

import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.map.MapPalette
import java.awt.image.BufferedImage

sealed class WorldAttributesState(private val m: DreamResourceReset, private val server: Server, val deathChestBackground: BufferedImage, val worldName: String) {
    val _world: World?
        get() = server.getWorld(worldName)

    val world: World
        get() = _world!!

    // We are going to keep them in memory to avoid creating them on the fly
    val lorittaDeathChestImage = create(m.lorittaImage)
    val pantufaDeathChestImage = create(m.pantufaImage)
    val gabrielaDeathChestImage = create(m.gabrielaImage)
    val powerDeathChestImage = create(m.powerImage)

    private fun create(image: BufferedImage): ByteArray {
        val finalImage = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
        val graphics = finalImage.createGraphics()
        graphics.drawImage(deathChestBackground, 0, 0, null)
        graphics.drawImage(image, 0, 0, null)
        return MapPalette.imageToBytes(finalImage)
    }

    abstract fun canYouLoseItems(): Boolean

    class ResourcesWorldAttributesState(m: DreamResourceReset, server: Server, deathChestBackground: BufferedImage) : WorldAttributesState(m, server, deathChestBackground, "Resources") {
        override fun canYouLoseItems() = world.time !in 0 until 13_000 // https://minecraft.fandom.com/wiki/Daylight_cycle
    }

    class NetherWorldAttributesState(m: DreamResourceReset, server: Server, deathChestBackground: BufferedImage) : WorldAttributesState(m, server, deathChestBackground, "Nether") {
        override fun canYouLoseItems() = true
    }

    class TheEndWorldAttributesState(m: DreamResourceReset, server: Server, deathChestBackground: BufferedImage) : WorldAttributesState(m, server, deathChestBackground, "TheEndSlk") {
        override fun canYouLoseItems() = true
    }
}