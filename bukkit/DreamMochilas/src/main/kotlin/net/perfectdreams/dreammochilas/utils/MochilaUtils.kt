package net.perfectdreams.dreammochilas.utils

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object MochilaUtils {
    val DEFAULT_MOCHILA_TITLE_NAME = Component.text("Mochila")
        .color(NamedTextColor.BLACK)
        .decoration(TextDecoration.ITALIC, false)

    private val isMagnet: (ItemStack?) -> Boolean = { it?.type == Material.STONE_HOE && it.hasItemMeta() && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData in 1 .. 2 }
    val HAS_MAGNET_KEY = SparklyNamespacedKey("has_magnet")
    val IS_FULL_KEY = SparklyNamespacedKey("is_backpack_full")

    val IS_MOCHILA_KEY = SparklyNamespacedKey("is_mochila")
    val MOCHILA_ID_KEY = SparklyNamespacedKey("mochila_id")

    val loadedMochilas = ConcurrentHashMap<Long, MochilaWrapper>()
    private val plugin = Bukkit.getPluginManager().getPlugin("DreamMochilas")!!
    private val mochilaDataLoadMutexes = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<Long, Mutex>()
        .asMap()

    val mochilaCreationMutex = Mutex()

    fun isMochilaItem(item: ItemStack) = item.type == Material.CHEST_MINECART && item.hasItemMeta() && item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData in 10..46
    fun isMochila(item: ItemStack) = isMochilaItem(item) && item.itemMeta.persistentDataContainer.has(IS_MOCHILA_KEY)

    fun getMochilaId(item: ItemStack): Long? = if (isMochila(item))
        item.itemMeta.persistentDataContainer.get(MOCHILA_ID_KEY, PersistentDataType.LONG)
    else
        null

    private fun getMutexForMochila(mochilaId: Long) = mochilaDataLoadMutexes.getOrPut(mochilaId) { Mutex() }

    /**
     * Retrieves the mochila from the database or, if it is loaded in memory, gets the already loaded mochila
     *
     * This will also increase the held lock count on the [MochilaWrapper] instance by 1, please use [MochilaWrapper.release] after you don't need the mochila anymore!
     *
     * @param mochilaId the mochila ID
     * @return the mochila object
     */
    suspend fun retrieveMochilaAndHold(mochilaId: Long, triggerType: String? = null): MochilaAccessHolder? {
        DreamUtils.assertAsyncThread(true)

        val mutex = getMutexForMochila(mochilaId)
        mutex.withLock {
            // Load from memory if it exists
            val memoryMochila = loadedMochilas[mochilaId]

            if (memoryMochila != null) {
                // Okay, so the memory mochila exists, we need to check something beforehand tho
                //
                // The mochila may be SAVING!! Do you know that!?!
                //
                // So we need to avoid using a mochila that doesn't have any held locks, because if it doesn't, MAYBE IT IS BEING USED FOR SAVING
                // You may ask "what is the issue of using a mochila that doesn't have any held locks smh"... well...
                // A RACE CONDITION WHERE PLUGINS ARE USING A "WAS-IN-MEMORY-BEFORE" MOCHILA, WHICH CAUSES DESYNC BETWEEN THE TRUTH X DATABASE
                // And you know what that can cause...?
                // A DUPE BUG!
                //
                // So what's the fix then?
                // We will only return the mochila IF and only IF the held locks count is NOT zero
                //
                // We also need to do some precautions, to avoid an instance where a thread may release a mochila, but at the same time another thread may ask for a hold lock
                // This would cause the mochila to be removed from the cache but an instance of it would be kept in memory
                // This is remedied by using the local mochila lock: If it is in a save process, the lock would be held and, when released, the holds will be 0
                memoryMochila.withLocalMochilaLock {
                    // THIS AIN'T THE PROPER WAY TO HANDLE THOSE HELD LOCKS BUT THERE ISN'T ANY OTHER WAY TO HANDLE THEM!!
                    if (memoryMochila.holds != 0) {
                        memoryMochila.holds++

                        plugin.logger.info { "Loaded backpack $mochilaId ($memoryMochila) from memory! Now the current held lock count is ${memoryMochila.holds}! Triggered by $triggerType" }

                        return MochilaAccessHolder(memoryMochila).also {
                            it.isHolding = true
                        }
                    }
                }

                plugin.logger.info { "Tried loading backpack $mochilaId ($memoryMochila) from memory but its held locks count is zero! To avoid dupe issues, we will pull the data from the database... Triggered by $triggerType" }
            }

            val mochila = transaction(Databases.databaseNetwork) {
                Mochila.find { Mochilas.id eq mochilaId }
                    .firstOrNull()
            }

            return if (mochila != null) {
                val mochilaWrapper = MochilaWrapper(plugin as DreamMochilas, mochila)
                plugin.logger.info { "Loaded backpack $mochilaId ($mochila/$mochilaWrapper) from database! Triggered by $triggerType" }
                loadedMochilas[mochilaId] = mochilaWrapper
                MochilaAccessHolder(mochilaWrapper).also { it.hold(triggerType) }
            } else {
                plugin.logger.info { "Tried loading backpack $mochilaId ($mochila) from database, but it doesn't exist! Triggered by $triggerType" }
                null
            }
        }
    }

    /**
     * Updates the [mochilaItem] metadata based on the [inventory]'s information
     */
    fun updateMochilaItemLore(inventory: Inventory, mochilaItem: ItemStack) {
        // Items can only be manipulated in the main thread, if else the server doesn't like it
        // (Race conditions where the server reads the ItemMeta while another thread is changing it, causing a full server crash)
        DreamUtils.assertMainThread(true)
        mochilaItem.meta<ItemMeta> {
            if (hasLore())
                with (lore!!) {
                    if (size == 3) add("\n")

                    val usedSize = with (inventory) { count { it != null }.let { if (it == -1) size else it } }
                    // Do a nice transition from green to red, depending on how many slots are used
                    val totalSizeInPercentage = usedSize / inventory.size.toDouble()
                    val color = ChatColor.of(Color(
                        0 + (200 * totalSizeInPercentage).toInt(),
                        255 - (255 * totalSizeInPercentage).toInt(),
                        125 - (125 * totalSizeInPercentage).toInt()
                    ))

                    val usedSlotsLine = with (inventory.size) {
                        "$color$usedSize/$this §7slots usados${if (usedSize == this) " §c§lCHEIA!" else ""}"
                    }

                    if (size == 4) add(usedSlotsLine) else set(4, usedSlotsLine)

                    with (persistentDataContainer) {
                        set(IS_FULL_KEY, PersistentDataType.BYTE, if (usedSize == inventory.size) 1 else 0)

                        inventory.firstOrNull(isMagnet)?.let {
                            set(HAS_MAGNET_KEY, PersistentDataType.BYTE, 1)
                            val magnetLastLine = it.lore!!.last()

                            if (size == 5) {
                                add("\n")
                                add("§6Mochila magnetizada")
                                add(magnetLastLine)
                            } else set(7, magnetLastLine)
                        } ?: run {
                            set(HAS_MAGNET_KEY, PersistentDataType.BYTE, 0)
                            if (size == 8) for (index in 7 downTo 5) removeAt(index)
                        }
                    }

                    lore = this
                }
        }
    }
}