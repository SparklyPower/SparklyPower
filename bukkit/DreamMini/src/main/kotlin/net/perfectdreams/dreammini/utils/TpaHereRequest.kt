package net.perfectdreams.dreammini.utils

import org.bukkit.entity.Player

data class TpaHereRequest(val playerThatRequestedTheTeleport: Player, val playerThatWillBeTeleported: Player)