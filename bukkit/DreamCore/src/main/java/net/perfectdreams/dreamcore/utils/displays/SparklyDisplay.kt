package net.perfectdreams.dreamcore.utils.displays

import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import java.util.*

// A SparklyDisplay may have multiple kinds of "DisplayBlocks"
class SparklyDisplay(
    val m: SparklyDisplayManager,
    val owner: Plugin,
    // THIS IS NOT AN ENTITY ID!!!
    val uniqueId: UUID,
    var locationReference: LocationReference,
) {
    val blocks = mutableListOf<DisplayBlock>()

    fun getSparklyDisplayUserId(): String? = m.m.sparklyUserDisplayManager.createdTextDisplays.values.firstOrNull {
        it.sparklyDisplay == this@SparklyDisplay
    }?.id

    fun getOwnerOfThisEntity(entity: Entity): DisplayBlock? {
        return blocks.firstOrNull { it.areWeTheOwnerOfThisEntity(entity) }
    }

    fun removeAllBlocks() {
        blocks.toList().forEach {
            it.remove()
        }
    }

    fun remove() {
        // First we remove all blocks
        removeAllBlocks()

        // Then we remove ourselves from the display list
        m.sparklyDisplays.remove(uniqueId)
    }

    fun getYLocationOfBlock(blockToBeChecked: DisplayBlock): Double {
        var currentY = locationReference.toBukkit().y

        for (block in blocks) {
            if (blockToBeChecked == block)
                break
            currentY -= block.getHeight()
        }

        return currentY
    }

    fun addDisplayBlock(): DisplayBlock.TextDisplayBlock {
        val newBlock = createDisplayBlock()

        blocks.add(newBlock)

        return newBlock
    }

    fun createDisplayBlock(): DisplayBlock.TextDisplayBlock {
        // Now THIS TIME we need to create a UUID for it
        val textDisplay = spawnDisplayBlock()

        return DisplayBlock.TextDisplayBlock(
            m,
            this,
            textDisplay?.uniqueId
        )
    }

    private fun spawnDisplayBlock(): Entity? {
        // Now THIS TIME we need to create a UUID for it
        val location = locationReference.toBukkit()
        if (!location.isWorldLoaded) {
            m.m.logger.warning("Tried spawning display block for SparklyDisplay $uniqueId (${getSparklyDisplayUserId()}), but the world isn't loaded! Ignoring...")
            return null
        }

        val textDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.persistentDataContainer.set(m.handledBySparklyDisplay, uniqueId.toString())

        return textDisplay
    }

    fun createItemDropDisplayBlock(itemStack: ItemStack): DisplayBlock.ItemDropDisplayBlock {
        val (textDisplay, item) = spawnItemDropDisplayBlock(itemStack)

        val newBlock = DisplayBlock.ItemDropDisplayBlock(
            m,
            this,
            itemStack,
            textDisplay?.uniqueId,
            item?.uniqueId
        )

        return newBlock
    }

    fun addItemDropDisplayBlock(itemStack: ItemStack): DisplayBlock.ItemDropDisplayBlock {
        val newBlock = createItemDropDisplayBlock(itemStack)

        blocks.add(newBlock)

        return newBlock
    }

    private fun spawnItemDropDisplayBlock(itemStack: ItemStack): Pair<Entity?, Entity?> {
        val location = locationReference.toBukkit()
        if (!location.isWorldLoaded) {
            m.m.logger.warning("Tried spawning item drop display block for SparklyDisplay $uniqueId (${getSparklyDisplayUserId()}), but the world isn't loaded! Ignoring...")
            return Pair(null, null)
        }

        val textDisplay = location.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.persistentDataContainer.set(m.handledBySparklyDisplay, uniqueId.toString())

        // If we don't set an text, it will be invisible (ooo spooky)
        // textDisplay.text(textComponent { content("*item drop*") })
        textDisplay.transformation = Transformation(
            textDisplay.transformation.translation,
            textDisplay.transformation.leftRotation,
            org.joml.Vector3f(
                0.1f,
                0.1f,
                0.1f
            ),
            textDisplay.transformation.rightRotation,
        )

        val item = location.world.dropItem(location, itemStack)
        item.persistentDataContainer.set(m.handledBySparklyDisplay, uniqueId.toString())

        textDisplay.addPassenger(item)
        item.setGravity(false)
        item.isUnlimitedLifetime = true
        item.setCanPlayerPickup(false)
        item.setCanMobPickup(false)
        item.health = Int.MAX_VALUE

        return Pair(textDisplay, item)
    }

    fun synchronizeBlocks() {
        val location = locationReference.toBukkit()
        if (location.world == null) {
            m.m.logger.info("Not synchronizing SparklyDisplay ${uniqueId} (${getSparklyDisplayUserId()}) because the location world is null!")
            return
        }

        // No need to synchronize blocks if the chunk ain't loaded (avoids spam with "Recreating entity" when the display's chunk is not loaded)
        // Blocks in unloaded chunks will be synchronized when the EntitiesLoadEvent is called
        if (!location.isChunkLoaded)
            return

        for (block in blocks) {
            synchronizeBlock(block)
        }
    }

    fun synchronizeBlock(block: DisplayBlock) {
        when (block) {
            is DisplayBlock.TextDisplayBlock -> {
                // TODO: Should the display blocks handle their own entities?
                var entity = block.getEntity()

                if (entity == null) {
                    m.m.logger.info("Recreating the TextDisplayBlock entity")

                    // Null, recreate the entity!
                    val textDisplay = spawnDisplayBlock() ?: return
                    entity = textDisplay
                }

                block.updateEntity(entity)
            }

            is DisplayBlock.ItemDropDisplayBlock -> {
                // TODO: Should the display blocks handle their own entities?
                var textDisplayEntity = block.getTextDisplayEntity()
                var itemDropEntity = block.getItemDropEntity()

                if (textDisplayEntity == null || itemDropEntity == null) {
                    // Null, recreate the entity!
                    textDisplayEntity?.remove()
                    itemDropEntity?.remove()

                    m.m.logger.info("Recreating the ItemDropDisplayBlock entities")

                    val newEntities = spawnItemDropDisplayBlock(block.itemStack)
                    textDisplayEntity = newEntities.first ?: return
                    itemDropEntity = newEntities.second ?: return
                }

                block.updateEntity(textDisplayEntity, itemDropEntity)
            }

            is DisplayBlock.SpacerDisplayBlock -> {}
        }
    }
}

/* sealed class SparklyDisplay(
    val m: SparklyDisplayManager,
    val owner: Plugin,
    val initialLocation: Location,
    val initialTransformation: Transformation,
    // We can't (and shouldn't!) store the entity reference, since the reference may change when the entity is despawned!
    // So we store the unique ID
    val uniqueId: UUID
) {
    var location: Location = initialLocation

    /**
     * Gets the display entity, this may be null if the entity is unloaded
     */
    abstract fun getEntity(): Entity?

    /**
     * Deletes the display entity from the world
     */
    abstract fun remove()

    /**
     * Synchronizes the entity [entity] with the currently stored data about the display entity
     */
    abstract fun updateEntity(entity: Entity)

    class SparklyTextDisplay(
        m: SparklyDisplayManager,
        owner: Plugin,
        initialLocation: Location,
        initialTransformation: Transformation,
        uniqueId: UUID
    ) : SparklyDisplay(m, owner, initialLocation, initialTransformation, uniqueId) {
        private var text: Component? = null
        var billboard: Display.Billboard = Display.Billboard.CENTER
            set(value) {
                field = value
                getAndUpdateEntity()
            }
        var transformation: Transformation = initialTransformation
            set(value) {
                field = value
                getAndUpdateEntity()
            }
        var isShadowed: Boolean = false
            set(value) {
                field = value
                getAndUpdateEntity()
            }
        var backgroundColor: Color = Color.fromARGB(1073741824)
            set(value) {
                field = value
                getAndUpdateEntity()
            }

        private var lineWidth = Int.MAX_VALUE

        override fun getEntity() = Bukkit.getEntity(uniqueId)

        override fun remove() {
            m.m.logger.info { "Removing display entity $uniqueId from the world and from the display entity storage..." }
            m.displayEntities.remove(uniqueId)
            getEntity()?.remove()
        }

        fun text(text: Component?) {
            this.text = text

            getAndUpdateEntity()
        }

        fun teleport(newLocation: Location) {
            val previousLocation = this.location
            this.location = newLocation

            // Because teleports are "I want this RIGHT NOW", we will handle them differently
            // We do this in this way because holograms may be in unloaded chunks, so teleporting them won't work
            m.m.launchMainThread {
                val previousLocationChunk = previousLocation.world.getChunkAtAsync(previousLocation).await()
                val entity = previousLocationChunk.entities.firstOrNull { it.uniqueId == uniqueId }

                if (entity != null)
                    updateEntity(entity)
            }
        }

        private fun getAndUpdateEntity() {
            val entity = getEntity() ?: return
            updateEntity(entity)
        }

        override fun updateEntity(entity: Entity) {
            val textDisplay = entity as? TextDisplay ?: error("Entity ${entity.uniqueId} is not a TextDisplay!")

            textDisplay.teleport(this.location)
            textDisplay.text(this.text)
            textDisplay.billboard = this.billboard
            textDisplay.lineWidth = this.lineWidth
            // textDisplay.transformation.scale.set(4f, 4f, 4f)
            textDisplay.transformation = this.transformation
            textDisplay.isShadowed = this.isShadowed
            textDisplay.backgroundColor = backgroundColor
        }
    }
} */