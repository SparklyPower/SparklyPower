package net.perfectdreams.dreamcore.utils.displays

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import net.perfectdreams.dreamcore.utils.get
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.*

class SparklyDisplayListener(val m: SparklyDisplayManager) : Listener {
    // TODO: Change this to EntitiesLoadEvent when Paper fixes the borked EntitiesLoadEvent call (the event isn't being fired in 1.21)
    @EventHandler
    fun onEntitiesLoad(event: ChunkLoadEvent) {
        for (entity in event.chunk.entities) {
            // Is this an NPC?
            val sparklyDisplayUniqueId = entity.persistentDataContainer.get(m.handledBySparklyDisplay)?.let { UUID.fromString(it) } ?: continue

            // It is handled by a SparklyDisplay, but does it exist?
            val sparklyDisplay = m.sparklyDisplays[sparklyDisplayUniqueId]

            if (sparklyDisplay == null) {
                // If not, we are going to delete it!
                m.m.logger.warning("Deleting entity ${entity.uniqueId} because there isn't any SparklyDisplay with ID $sparklyDisplayUniqueId")

                // Bail out!
                entity.remove()
            } else {
                // Are we still handled by the SparklyDisplay instance?
                val displayBlockOfThisEntity = sparklyDisplay.getOwnerOfThisEntity(entity)

                // This may happen when teleporting an entity from an unloaded chunk to a loaded chunk, because in this case SparklyDisplay will create a new entity
                if (displayBlockOfThisEntity == null) {
                    // If not, we are going to delete it!
                    m.m.logger.warning("Deleting entity ${entity.uniqueId} because while there is a SparklyDisplay with ID $sparklyDisplayUniqueId, they say that they aren't handling that entity!")

                    // Bail out!
                    entity.remove()
                } else {
                    // We are still valid, yay!!! (insert trans joke *you are valid* here, trans people are valid ok)
                    // And because we are valid, let's synchronize the block!
                    sparklyDisplay.synchronizeBlock(displayBlockOfThisEntity)
                }
            }
        }
    }

    @EventHandler
    fun onDisable(event: PluginDisableEvent) {
        // Here's the thing: If we are disabling OURSELVES, we NEED to save the data BEFORE we remove the displays from the map
        // println("plugin: ${event.plugin} ${m.m} ${event.plugin == m.m}")
        if (m.m.sparklyUserDisplayManager.configHasBeenLoaded && event.plugin == m.m) {
            m.m.sparklyUserDisplayManager.save()
        }

        m.sparklyDisplays.forEach { (id, data) ->
            if (data.owner == event.plugin) {
                // Delete all displays when the plugin is disabled
                data.remove()
            }
        }
    }

    // EntityRemoveFromWorldEvent does not fit our needs because it includes chunk unloading
    /* @EventHandler
    fun onDeath(event: EntityRemoveFromWorldEvent) {
        // If a display part dies, then we have very very very bad issues happening
        val sparklyDisplayUniqueId = event.entity.persistentDataContainer.get(m.handledBySparklyDisplay)?.let { UUID.fromString(it) } ?: return

        // It is handled by a SparklyDisplay, but does it exist?
        val sparklyDisplay = m.sparklyDisplays[sparklyDisplayUniqueId]

        if (sparklyDisplay == null) {
            m.m.logger.info("SparklyDisplay entity ${event.entity.uniqueId} died, but there isn't any SparklyDisplay with ID $sparklyDisplayUniqueId, this is *probably* not an issue but... keep an eye out for anything sus")
            return
        }

        sparklyDisplay.blocks.forEach {
            when (it) {
                is DisplayBlock.ItemDropDisplayBlock -> {
                    if (it.textDisplayUniqueId == event.entity.uniqueId || it.itemDropUniqueId == event.entity.uniqueId) {
                        if (!it.isRemoved) {
                            // Uh oh...
                            m.m.logger.warning("SparklyDisplay entity ${event.entity.uniqueId} died, but their display block wasn't removed from SparklyDisplay $sparklyDisplayUniqueId! This will cause inconsistencies and issues!!")
                        }
                    }
                }
                is DisplayBlock.SpacerDisplayBlock -> {}
                is DisplayBlock.TextDisplayBlock -> {
                    if (it.uniqueId == event.entity.uniqueId) {
                        if (!it.isRemoved) {
                            // Uh oh...
                            m.m.logger.warning("SparklyDisplay entity ${event.entity.uniqueId} died, but their display block wasn't removed from SparklyDisplay $sparklyDisplayUniqueId! This will cause inconsistencies and issues!!")
                        }
                    }
                }
            }
        }
    } */
}