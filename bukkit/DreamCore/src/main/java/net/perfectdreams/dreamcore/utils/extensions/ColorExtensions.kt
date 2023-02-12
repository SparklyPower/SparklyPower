package net.perfectdreams.dreamcore.utils.extensions

import java.awt.Color

private fun Int.toMCColorComponent() =
    this.toString(16).padStart(2, '0').map { "§$it" }.joinToString("")

fun Color.toMinecraftColor() =
    "§x" + this.red.toMCColorComponent() + this.green.toMCColorComponent() + this.blue.toMCColorComponent()

fun Color.changeSaturation(saturation: Float): Color {
    val hsbComponents = Color.RGBtoHSB(this.red, this.green, this.blue, null)
    val rgbInt = Color.HSBtoRGB(hsbComponents[0], saturation, hsbComponents[2])
    return Color(rgbInt)
}