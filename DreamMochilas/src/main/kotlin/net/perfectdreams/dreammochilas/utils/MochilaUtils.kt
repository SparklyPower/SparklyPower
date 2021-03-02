package net.perfectdreams.dreammochilas.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

object MochilaUtils {
    val loadedMochilas = ConcurrentHashMap<Long, Mochila>()
    val plugin = Bukkit.getPluginManager().getPlugin("DreamMochilas")!!
    val mochilaLoadSaveMutex = Mutex()
    val mochilaCreationMutex = Mutex()

    /**
     * Retrieves the mochila from the database or, if it is loaded in memory, gets the already loaded mochila
     *
     * @param mochilaId the mochila ID
     * @return the mochila object
     */
    suspend fun retrieveMochila(mochilaId: Long, triggerType: String? = null): Mochila? {
        DreamUtils.assertAsyncThread(true)

        plugin.logger.info { "Loading backpack $mochilaId, triggered by $triggerType; Is mutex locked? ${mochilaLoadSaveMutex.isLocked}" }

        mochilaLoadSaveMutex.withLock {
            // Load from memory if it exists
            val memoryMochila = loadedMochilas[mochilaId]

            if (memoryMochila != null) {
                plugin.logger.info { "Loaded backpack $mochilaId from memory! Triggered by $triggerType" }
                return memoryMochila
            }

            val mochila = transaction(Databases.databaseNetwork) {
                Mochila.find { Mochilas.id eq mochilaId }
                    .firstOrNull()
            }

            if (mochila != null) {
                plugin.logger.info { "Loaded backpack $mochilaId from database! Triggered by $triggerType" }
                loadedMochilas[mochilaId] = mochila
            } else {
                plugin.logger.info { "Tried loading backpack $mochilaId from database, but it doesn't exist! Triggered by $triggerType" }
            }

            return mochila
        }
    }

    /**
     * Saves the mochila to the database, but ONLY if the mochila inventory doesn't have any viewers AND mochilaInventoryManipulationLock is not locked
     *
     * @param mochila the mochila object
     */
    suspend fun saveMochila(mochila: Mochila, triggerType: String? = null) {
        DreamUtils.assertAsyncThread(true)

        val viewerCount = mochila.getOrCreateMochilaInventory().viewers.size
        val isManipulationLocked = mochila.mochilaInventoryManipulationLock.isLocked

        plugin.logger.info { "Saving backpack ${mochila.id.value}, triggered by $triggerType; Is mutex locked? ${mochilaLoadSaveMutex.isLocked}; Viewer Count: $viewerCount; Is Manipulation Locked? $isManipulationLocked" }

        if (viewerCount > 1) {
            plugin.logger.info { "Not going to save backpack ${mochila.id.value} on database because there's $viewerCount looking at it! Triggered by $triggerType" }
            return
        }

        if (isManipulationLocked) {
            plugin.logger.info { "Not going to save backpack ${mochila.id.value} on database because it is locked for manipulation! Triggered by $triggerType" }
            return
        }

        // Unless if we are running this in the event itself (impossible because needs to be in a async task), this will be 0
        if (0 == viewerCount && !isManipulationLocked) {
            mochilaLoadSaveMutex.withLock {
                // Save ONLY if there's less (or equal to) one viewer
                // The reason there's a 1 >= check is because on InventoryCloseEvent the inventory is not closed yet
                val inventory = mochila.cachedInventory

                // If the inventory is null, then it means that the inventory wasn't manipulated, so we don't need to save it
                if (inventory != null) {
                    plugin.logger.info { "Saving backpack ${mochila.id.value} on database! Triggered by $triggerType" }

                    transaction(Databases.databaseNetwork) {
                        mochila.content = inventory.toBase64(1)
                    }

                    plugin.logger.info { "Saved backpack ${mochila.id.value} on database! Triggered by $triggerType" }
                } else {
                    plugin.logger.info { "Decided not to save backpack ${mochila.id.value} because the inventory is null! Triggered by $triggerType" }
                }

                // Remove from memory
                plugin.logger.info { "Removing backpack ${mochila.id.value} from memory, triggered by $triggerType" }
                loadedMochilas.remove(mochila.id.value)
            }
        } else {
            plugin.logger.info { "Not going to save backpack ${mochila.id.value} on database! Triggered by $triggerType" }
        }
    }

    suspend fun removeCachedMochila(mochila: Mochila, triggerType: String? = null) {
        plugin.logger.info { "Removing backpack ${mochila.id.value} from cache (no save), triggered by $triggerType; Is mutex locked? ${mochilaLoadSaveMutex.isLocked}" }

        mochilaLoadSaveMutex.withLock {
            loadedMochilas.remove(mochila.id.value)
        }
    }

    suspend fun storeCachedMochila(mochila: Mochila) {
        mochilaLoadSaveMutex.withLock {
            loadedMochilas[mochila.id.value] = mochila
        }
    }
}