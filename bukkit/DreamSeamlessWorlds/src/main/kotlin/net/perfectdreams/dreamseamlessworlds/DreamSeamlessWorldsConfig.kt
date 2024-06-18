package net.perfectdreams.dreamseamlessworlds

import kotlinx.serialization.Serializable

@Serializable
data class DreamSeamlessWorldsConfig(
    val seamlessWorlds: Map<String, SeamlessWorldConfig>
) {
    @Serializable
    data class SeamlessWorldConfig(
        val northWorld: String? = null,
        val southWorld: String? = null,
        val eastWorld: String? = null,
        val westWorld: String? = null,
    )
}