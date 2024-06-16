package net.perfectdreams.pantufa.api.minecraft

import java.util.UUID

data class MinecraftAccountInfo(
    val uniqueId: UUID,
    val username: String
)
