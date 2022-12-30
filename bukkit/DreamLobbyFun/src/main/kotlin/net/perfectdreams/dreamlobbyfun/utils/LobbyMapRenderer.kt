package net.perfectdreams.dreamlobbyfun.utils

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Color

class LobbyMapRenderer(
    private val m: DreamLobbyFun,
    private val xIndex: Int,
    private val yIndex: Int
) : MapRenderer() {
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        val subImage = m.lobbyImage.getSubimage(xIndex * 128, yIndex * 128, 128, 128)

        var x = 0
        var y = 0
        for (byte in MapPalette.imageToBytes(subImage)) {
            if (x == 128) {
                y++
                x = 0
            }
            canvas.setPixel(x, y, byte)
            x++
        }
    }
}