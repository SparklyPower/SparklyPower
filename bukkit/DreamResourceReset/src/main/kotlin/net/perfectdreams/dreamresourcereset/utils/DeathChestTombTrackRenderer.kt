package net.perfectdreams.dreamresourcereset.utils

import org.bukkit.entity.Player
import org.bukkit.map.*

class DeathChestTombTrackRenderer(private val deathChestInfoData: DeathChestInfoData) : MapRenderer() {
    companion object {
        private val font = MinecraftFont()
        private val orangeShade = MapPalette.matchColor(216, 127, 51)
    }

    var alreadyAddedCursor = false
    var cursor = MapCursor(0, 0, 0, MapCursor.Type.RED_X, true)

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if (!alreadyAddedCursor) {
            canvas.cursors.addCursor(cursor)
            alreadyAddedCursor = true
        }

        // All shades have three variants, that's why we can subtract to get a darker shade!
        drawTextWithShadow(canvas, 0, 0, "§${orangeShade - 2};X: §44;${deathChestInfoData.x}", "§${orangeShade};X: §32;${deathChestInfoData.x}")
        drawTextWithShadow(canvas, 0, 10, "§${orangeShade - 2};Y: §44;${deathChestInfoData.y}", "§${orangeShade};Y: §32;${deathChestInfoData.y}")
        drawTextWithShadow(canvas, 0, 20, "§${orangeShade - 2};Z: §44;${deathChestInfoData.z}", "§${orangeShade};Z: §32;${deathChestInfoData.z}")
    }

    private fun drawTextWithShadow(
        canvas: MapCanvas,
        x: Int,
        y: Int,
        shadow: String,
        text: String
    ) {
        canvas.drawText(x, y + 1, font, shadow)
        canvas.drawText(x, y, font, text)
    }
}