package net.perfectdreams.dreamcore.utils.displays.user

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.utils.LocationReference
import org.bukkit.entity.Display

@Serializable
class UserCreatedSparklyDisplayData(
    val id: String,
    val location: LocationReference,
    val blocks: List<UserCreatedDisplayBlock>
) {
    @Serializable
    sealed class UserCreatedDisplayBlock {
        @Serializable
        class UserCreatedTextDisplayBlock(
            val text: String?,
            val billboard: Display.Billboard,
            val transformation: Transformation,
            val isShadowed: Boolean,
            val backgroundColor: Int,
            val lineWidth: Int
        ) : UserCreatedDisplayBlock()

        @Serializable
        class UserCreatedItemDropDisplayBlock(
            val itemStack: ByteArray
        ) : UserCreatedDisplayBlock()
    }

    @Serializable
    data class Vector3f(
        val x: Float,
        val y: Float,
        val z: Float
    )

    @Serializable
    data class Quaternionf(
        val x: Float,
        val y: Float,
        val z: Float,
        val w: Float
    )

    @Serializable
    data class Transformation(
        val translation: Vector3f,
        val leftRotation: Quaternionf,
        val scale: Vector3f,
        val rightRotation: Quaternionf
    )
}