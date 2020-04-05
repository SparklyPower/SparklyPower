package net.perfectdreams.dreamwarps.utils

import net.perfectdreams.dreamcore.utils.DreamMenu
import org.bukkit.entity.Player

interface WarpMenu {
    fun generateMenu(player: Player): DreamMenu
}