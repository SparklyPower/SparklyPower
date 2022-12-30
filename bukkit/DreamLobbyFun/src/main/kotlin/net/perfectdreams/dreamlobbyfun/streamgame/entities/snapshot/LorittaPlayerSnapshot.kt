package net.perfectdreams.dreamlobbyfun.streamgame.entities.snapshot

import net.perfectdreams.dreamlobbyfun.streamgame.entities.LorittaPlayer
import net.perfectdreams.dreamlobbyfun.streamgame.entities.PlayerMovementState

data class LorittaPlayerSnapshot(
    val type: LorittaPlayer.PlayerType,
    val x: Int,
    val y: Int,
    val speed: Double,
    val movementState: PlayerMovementState
)