package net.perfectdreams.dreamkits

import net.md_5.bungee.api.ChatColor
import java.awt.Color

fun main() {
    val input = "閪閪閪閪閪閪閪閪閪閪閪閪閪閪閪閪閪閪"

    var end = ""
    var hue = 0f

    for (x in input) {
        hue += 5f
        end += ChatColor.of(Color.getHSBColor(hue / 100f, 100f / 100f, 100f / 100f))
        end += x
    }

    println(end.replace("§", "&"))
}