package net.perfectdreams.dreamcore.utils.skins

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class StoredDatabaseSkin {
    abstract val configuredAt: Instant

    @Serializable
    sealed class MojangSkin : StoredDatabaseSkin() {
        abstract val mojangUUID: String
        abstract val value: String
        abstract val signature: String
    }

    @Serializable
    class SelfMojangSkin(
        override val mojangUUID: String,
        override val configuredAt: Instant,
        override val value: String,
        override val signature: String
    ) : MojangSkin()

    @Serializable
    class CustomMojangSkin(
        override val mojangUUID: String,
        override val configuredAt: Instant,
        override val value: String,
        override val signature: String
    ) : MojangSkin()

    @Serializable
    class NoSkin(override val configuredAt: Instant) : StoredDatabaseSkin()
}