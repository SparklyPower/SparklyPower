package net.perfectdreams.dreamcore.utils.displays

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SparklyDisplayManager(val m: DreamCore) {
    val handledBySparklyDisplay = SparklyNamespacedKey("handled_by_sparklydisplay", PersistentDataType.STRING)
    val sparklyDisplays = ConcurrentHashMap<UUID, SparklyDisplay>()

    fun start() {
        m.registerEvents(SparklyDisplayListener(this))

        // Create look close and remove outdated teams
        m.launchMainThread {
            // This is the same code as the one in the SparklyNPCListener EntitiesLoadEvent listener
            // Why it is here? Because entities may have been loaded BEFORE DreamCore has started
            // TODO: This needs to be updated to filter by blocks
            for (world in Bukkit.getWorlds()) {
                for (entity in world.entities) {
                    // Is this an NPC?
                    val sparklyDisplayUniqueId = entity.persistentDataContainer.get(handledBySparklyDisplay)?.let { UUID.fromString(it) } ?: continue

                    // It is handled by a SparklyDisplay, but does it exist?
                    val sparklyDisplay = sparklyDisplays[sparklyDisplayUniqueId]

                    if (sparklyDisplay == null) {
                        // If not, we are going to delete it!
                        m.logger.warning("Deleting entity ${entity.uniqueId} because there isn't any SparklyDisplay with ID $sparklyDisplayUniqueId")

                        // Bail out!
                        entity.remove()
                    }
                }
            }
        }
    }

    fun spawnDisplay(
        owner: Plugin,
        location: Location
    ): SparklyDisplay {
        return spawnDisplay(owner, LocationReference.fromBukkit(location))
    }

    fun spawnDisplay(
        owner: Plugin,
        location: LocationReference
    ): SparklyDisplay {
        val displayId = UUID.randomUUID()
        val sparklyDisplay = SparklyDisplay(
            this,
            owner,
            displayId,
            location
        )

        this.sparklyDisplays[displayId] = sparklyDisplay

        m.logger.info { "SparklyDisplay created! Their SparklyDisplay Unique ID is $displayId" }

        return sparklyDisplay
    }
}