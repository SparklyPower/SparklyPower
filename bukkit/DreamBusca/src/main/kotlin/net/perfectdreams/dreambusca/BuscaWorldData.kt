package net.perfectdreams.dreambusca

import kotlinx.serialization.Serializable

@Serializable
data class BuscaWorldData(
    val inhabitedChunkTimers: MutableMap<Long, Long>
)