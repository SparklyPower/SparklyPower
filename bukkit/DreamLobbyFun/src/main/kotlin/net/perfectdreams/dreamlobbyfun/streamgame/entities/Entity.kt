package net.perfectdreams.dreamlobbyfun.streamgame.entities

import net.perfectdreams.dreamlobbyfun.streamgame.GameState
import net.perfectdreams.dreamlobbyfun.streamgame.entities.snapshot.LorittaPlayerSnapshot
import java.util.UUID

abstract class Entity(
    val id: UUID,
    val m: GameState,
    var x: Int,
    var y: Int
) {
    var dead = false
    abstract val width: Int
    abstract val height: Int

    abstract fun tick()

    abstract fun remove()

    abstract fun snapshot(): LorittaPlayerSnapshot
}