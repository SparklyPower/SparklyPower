package net.perfectdreams.dreamchunkloader.data

import kotlinx.serialization.Serializable

@Serializable
data class ChunkLoaderBlockInfo(
    val active: Boolean,
    val power: Int
)