package net.perfectdreams.dreamlobbyfun.streamgame

import net.perfectdreams.dreamlobbyfun.streamgame.entities.snapshot.LorittaPlayerSnapshot
import java.util.*

data class GameEntitiesSnapshot(
    val takenAt: Long,
    val tick: Int,
    val entities: Map<UUID, LorittaPlayerSnapshot>
)