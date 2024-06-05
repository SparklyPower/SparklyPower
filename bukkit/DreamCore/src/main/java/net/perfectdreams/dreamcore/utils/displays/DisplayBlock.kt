package net.perfectdreams.dreamcore.utils.displays

import io.papermc.paper.entity.TeleportFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

sealed class DisplayBlock {
    abstract fun getHeight(): Double
    abstract fun remove()
    abstract fun areWeTheOwnerOfThisEntity(entity: Entity): Boolean

    /**
     *  A display block that its only purpose is to be used as a "spacer" between lines, if the user wants to add more padding between lines without using empty text displays
     */
    class SpacerDisplayBlock(
        val m: SparklyDisplayManager,
        val parent: SparklyDisplay,
        private val _height: Double
    ) : DisplayBlock() {
        override fun getHeight() = _height

        override fun remove() {
            parent.blocks.remove(this)
        }

        override fun areWeTheOwnerOfThisEntity(entity: Entity) = false
    }

    class ItemDropDisplayBlock(
        val m: SparklyDisplayManager,
        val parent: SparklyDisplay,
        itemStack: ItemStack,
        // We can't (and shouldn't!) store the entity reference, since the reference may change when the entity is despawned!
        // So we store the unique ID
        var textDisplayUniqueId: UUID?,
        var itemDropUniqueId: UUID?
    ) : DisplayBlock() {
        override fun getHeight() = 0.7
        var isRemoved = false

        var itemStack: ItemStack = itemStack
            set(value) {
                field = value

                parent.synchronizeBlocks()
            }

        override fun remove() {
            m.m.logger.info { "Removing item drop display $textDisplayUniqueId and $itemDropUniqueId from the world and from the display entity storage..." }
            isRemoved = true
            parent.blocks.remove(this)
            getTextDisplayEntity()?.remove()
            getItemDropEntity()?.remove()
        }

        fun getTextDisplayEntity() = textDisplayUniqueId?.let { Bukkit.getEntity(it) }
        fun getItemDropEntity() = itemDropUniqueId?.let { Bukkit.getEntity(it) }
        override fun areWeTheOwnerOfThisEntity(entity: Entity) = entity.uniqueId == textDisplayUniqueId || entity.uniqueId == itemDropUniqueId

        fun updateEntity(textDisplayEntity: Entity, itemDropEntity: Entity) {
            check(textDisplayEntity is TextDisplay) { "Entity ${textDisplayEntity.uniqueId} is not a TextDisplay!" }
            check(itemDropEntity is Item) { "Entity ${itemDropEntity.uniqueId} is not a Item!" }

            this.textDisplayUniqueId = textDisplayEntity.uniqueId
            this.itemDropUniqueId = itemDropEntity.uniqueId

            itemDropEntity.itemStack = itemStack

            textDisplayEntity.teleport(
                this.parent
                    .locationReference
                    .toBukkit()
                    .clone()
                    .apply {
                        // we need to add the transformation scale y offset because holograms are offsetted by the "bottom" not the "top"
                        y = parent.getYLocationOfBlock(this@ItemDropDisplayBlock) - getHeight()
                    },
                TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_PASSENGERS,
            )
        }
    }

    class TextDisplayBlock(
        val m: SparklyDisplayManager,
        val parent: SparklyDisplay,
        // We can't (and shouldn't!) store the entity reference, since the reference may change when the entity is despawned!
        // So we store the unique ID
        var uniqueId: UUID?
    ) : DisplayBlock() {
        var currentText: Component? = null
            private set
        var billboard: Display.Billboard = Display.Billboard.CENTER
            set(value) {
                field = value

                parent.synchronizeBlocks()
            }
        var transformation: Transformation = Transformation(
            Vector3f(0f, 0f, 0f),
            Quaternionf(1f, 0f, 0f, 0f),
            Vector3f(1f, 1f, 1f),
            Quaternionf(1f, 0f, 0f, 0f)
        )
            set(value) {
                field = value

                parent.synchronizeBlocks()
            }

        var isShadowed: Boolean = false
            set(value) {
                field = value

                parent.synchronizeBlocks()
            }

        var backgroundColor: Color = Color.fromARGB(1073741824)
            set(value) {
                field = value

                parent.synchronizeBlocks()
            }

        var isRemoved = false
        // Unused for now
        var lineWidth = Int.MAX_VALUE
            private set

        fun getEntity() = uniqueId?.let { Bukkit.getEntity(it) }
        override fun areWeTheOwnerOfThisEntity(entity: Entity) = entity.uniqueId == uniqueId

        override fun remove() {
            m.m.logger.info { "Removing display entity $uniqueId from the world and from the display entity storage..." }
            isRemoved = true
            parent.blocks.remove(this)
            getEntity()?.remove()
        }

        fun text(text: Component?) {
            this.currentText = text

            parent.synchronizeBlocks()
        }

        private fun getAndUpdateEntity() {
            val entity = getEntity() ?: return
            updateEntity(entity)
        }

        fun updateEntity(entity: Entity) {
            val textDisplay = entity as? TextDisplay ?: error("Entity ${entity.uniqueId} is not a TextDisplay!")

            this.uniqueId = entity.uniqueId

            textDisplay.teleport(
                this.parent
                    .locationReference
                    .toBukkit()
                    .clone()
                    .apply {
                        // we need to add the transformation scale y offset because holograms are offsetted by the "bottom" not the "top"
                        y = (parent.getYLocationOfBlock(this@TextDisplayBlock) - getHeight())
                    }
            )

            // textDisplay.teleport(this.location)
            textDisplay.text(this.currentText)
            textDisplay.billboard = this.billboard
            textDisplay.lineWidth = this.lineWidth
            // textDisplay.transformation.scale.set(4f, 4f, 4f)
            textDisplay.transformation = this.transformation
            textDisplay.isShadowed = this.isShadowed
            textDisplay.backgroundColor = backgroundColor
        }

        // Bukkit does not provide a way to get the height of a text display because its hitbox is 0
        override fun getHeight(): Double {
            val text = this.currentText ?: return 0.0

            val content = PlainTextComponentSerializer.plainText().serialize(text)
            // Very hacky!!!
            return (0.3 * transformation.scale.y) * (content.count { it == '\n' } + 1)
        }
    }
}