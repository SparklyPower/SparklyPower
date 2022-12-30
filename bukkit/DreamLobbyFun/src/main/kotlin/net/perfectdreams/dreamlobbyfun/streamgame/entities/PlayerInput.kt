package net.perfectdreams.dreamlobbyfun.streamgame.entities

sealed class PlayerInput {
    class Jump(val power: Double) : PlayerInput()
    object Left : PlayerInput()
    object Right : PlayerInput()
}