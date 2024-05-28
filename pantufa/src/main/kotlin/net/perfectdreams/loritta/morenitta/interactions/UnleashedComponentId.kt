package net.perfectdreams.loritta.morenitta.interactions

import java.util.UUID

class UnleashedComponentId(val uniqueId: UUID) {
    companion object {
        const val UNLEASHED_COMPONENT_PREFIX = "unleashed"

        operator fun invoke(componentIdWithPrefix: String): UnleashedComponentId {
            require(componentIdWithPrefix.startsWith("$UNLEASHED_COMPONENT_PREFIX:")) { "Not a Unleashed Component ID!" }
            return UnleashedComponentId(UUID.fromString(componentIdWithPrefix.substringAfterLast(":")))
        }
    }

    override fun toString() = "$UNLEASHED_COMPONENT_PREFIX:$uniqueId"
}