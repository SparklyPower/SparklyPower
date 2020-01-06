package net.perfectdreams.dreamtrails.utils

import net.perfectdreams.dreamcore.utils.DreamUtils

data class ColoredArmorData(
    var r: ArmorColor,
    var g: ArmorColor,
    var b: ArmorColor
) {
    data class ArmorColor(
        var value: Int,
        var forward: Boolean
    ) {
        fun addAndGet(): Int {
            val randomValue = DreamUtils.random.nextInt(0, 11)
            if (forward) {
                value += randomValue
            } else {
                value -= randomValue
            }

            if (value >= 255) {
                forward = false
                value = 255
            } else if (0 >= value) {
                forward = true
                value = 0
            }

            return value
        }
    }
}