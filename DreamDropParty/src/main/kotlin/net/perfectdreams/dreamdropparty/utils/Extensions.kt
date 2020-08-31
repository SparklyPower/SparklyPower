package net.perfectdreams.dreamdropparty.utils

import org.bukkit.ChatColor
import org.bukkit.boss.BarColor

fun BarColor.toChatColor(): ChatColor {
    return when (this) {
        BarColor.BLUE -> ChatColor.BLUE
        BarColor.GREEN -> ChatColor.GREEN
        BarColor.PINK -> ChatColor.LIGHT_PURPLE
        BarColor.PURPLE -> ChatColor.DARK_PURPLE
        BarColor.RED -> ChatColor.RED
        BarColor.WHITE -> ChatColor.WHITE
        BarColor.YELLOW -> ChatColor.YELLOW
    }
}