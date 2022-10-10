package net.perfectdreams.dreammini.utils

import org.bukkit.entity.Player

data class TpaRequest(val playerThatRequestedTheTeleport: Player, val playerThatWillBeTeleported: Player)