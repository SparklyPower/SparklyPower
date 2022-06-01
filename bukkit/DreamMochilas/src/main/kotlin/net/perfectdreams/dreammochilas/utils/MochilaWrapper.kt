package net.perfectdreams.dreammochilas.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.fromBase64Inventory
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class MochilaWrapper(
    private val plugin: DreamMochilas,
    val mochila: Mochila
) {
    var holds = 0
    val holdingLocksAndSavingMochilaMutex = Mutex()

    suspend fun heldLocksCount() = withLocalMochilaLock { holds }

    suspend fun hold(triggerType: String? = null) = withLocalMochilaLock {
        holds++
        plugin.logger.info { "Holding lock for mochila ${mochila.id.value} ($this) and currently there are $holds held locks! Triggered by $triggerType" }
    }

    suspend fun release(triggerType: String? = null) = withLocalMochilaLock {
        DreamUtils.assertAsyncThread(true)

        holds--
        plugin.logger.info { "Releasing lock for mochila ${mochila.id.value} ($this) and currently there are $holds held locks! Triggered by $triggerType" }

        if (holds == 0) {
            plugin.logger.info { "There are zero locks holding ${mochila.id.value} ($this), so we will save the mochila to the database and remove it from our cache! Triggered by $triggerType" }
            saveMochila(triggerType)
        }
    }

    suspend inline fun <reified T> withLocalMochilaLock(block: () -> (T)) = holdingLocksAndSavingMochilaMutex.withLock(action = block)

    private val mochilaInventoryCreationLock = Mutex()
    private var cachedInventory: Inventory? = null

    suspend fun getOrCreateMochilaInventory(title: Component = MochilaUtils.DEFAULT_MOCHILA_TITLE_NAME): Inventory {
        // We need to lock to avoid two threads loading the inventory at the same time, causing issues
        mochilaInventoryCreationLock.withLock {
            return cachedInventory ?: run {
                val blahInventory = mochila.content.fromBase64Inventory() // Vamos pegar o inventário original

                val mochilaSize = 54.coerceAtMost(mochila.size)
                val guiTexture = when (mochilaSize) {
                    27 -> "\uE256"
                    36 -> "\uE257"
                    45 -> "\uE258"
                    else -> "\uE255"
                }

                // E criar ele com o nosso holder personalizado
                val inventory = Bukkit.createInventory(
                    MochilaInventoryHolder(),
                    mochilaSize,
                    Component.text("ꈉ${guiTexture}陇")
                        .color(NamedTextColor.WHITE)
                        .append(
                            Component.empty()
                                .color(NamedTextColor.BLACK)
                                .append(title)
                        )
                )

                val blahInventoryContents = blahInventory.contents
                if (blahInventoryContents != null) {
                    // When serializing, the items are stored as "ItemStack?" if it is AIR
                    // So we are going to workaround this by replacing all null values with a AIR ItemStack!
                    inventory.setContents(
                        blahInventoryContents.map {
                            it ?: ItemStack(Material.AIR)
                        }.toTypedArray()
                    )
                }

                cachedInventory = inventory
                return inventory
            }
        }
    }

    fun saveMochila(
        triggerType: String? = null,
        bypassAssertAsyncThreadCheck: Boolean = false,
        removeFromMemory: Boolean = true
    ) {
        // We need to bypass when shutting down the server
        if (!bypassAssertAsyncThreadCheck)
            DreamUtils.assertAsyncThread(true)

        val cachedInventory = cachedInventory
        if (cachedInventory == null) {
            plugin.logger.info { "Not going to save backpack ${mochila.id.value} ($this) on database because there isn't a cached inventory present, so its content weren't modified! Triggered by $triggerType" }
        } else {
            plugin.logger.info { "Saving backpack ${mochila.id.value} ($this) on database! Triggered by $triggerType" }

            transaction(Databases.databaseNetwork) {
                mochila.content = cachedInventory.toBase64(1)
            }

            plugin.logger.info { "Saved backpack ${mochila.id.value} ($this) on database! Triggered by $triggerType" }
        }

        if (removeFromMemory) {
            // Remove from memory after successful database save/no save
            // This method should ONLY be ran after all holds are released, so this shouldn't cause any issues... well, that's what I hope :S
            plugin.logger.info { "Removing backpack ${mochila.id.value} ($this) from memory, triggered by $triggerType" }
            MochilaUtils.loadedMochilas.remove(mochila.id.value)
        }
    }
}