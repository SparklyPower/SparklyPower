package net.perfectdreams.dreammapwatermarker.map

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView

class ImgRenderer(private val image: ByteArray) : MapRenderer() {
    var finished = false

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if (!finished) {
            // Because we can't set the buffer directly in the MapCanvas...
            var x = 0
            var y = 0
            for (byte in image) {
                if (x == 128) {
                    y++
                    x = 0
                }
                canvas.setPixel(x, y, byte)
                x++
            }
            finished = true
        }
    }
}