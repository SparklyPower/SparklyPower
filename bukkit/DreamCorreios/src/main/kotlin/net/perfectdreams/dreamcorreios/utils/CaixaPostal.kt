package net.perfectdreams.dreamcorreios.utils

import dev.forst.exposed.insertOrUpdate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreamcorreios.tables.ContaCorreios
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CaixaPostal(
    val m: DreamCorreios,
    val playerId: UUID,
    val items: MutableList<ItemStack>
) {
    private val accessHolders = mutableListOf<CaixaPostalAccessHolder>()
    private val accessHolderMutex = Mutex()
    private val pendingAddedItems = mutableListOf<ItemStack>()

    suspend fun createAccess() = accessHolderMutex.withLock {
        val accessHolder = CaixaPostalAccessHolder(this)
        accessHolders.add(accessHolder)
        return@withLock accessHolder
    }

    suspend fun releaseAccess(accessHolder: CaixaPostalAccessHolder) = accessHolderMutex.withLock {
        accessHolders.remove(accessHolder)
        saveIfNotLocked()
    }

    fun addItems(vararg itemStacks: ItemStack) {
        // To avoid replacing added items, we will only add the items when it is time to save the inventory
        // This way we avoid bugs that may arise by "omg I kept the mailbox open while I received a item and now it disappeared!"
        // TODO: Merge items?
        pendingAddedItems.addAll(itemStacks)
    }

    private suspend fun saveIfNotLocked() {
        m.loadingAndUnloadingCaixaPostalMutex.withLock {
            if (accessHolders.size != 0) {
                m.logger.info { "Not saving caixa postal $playerId because there are still ${accessHolders.size} access holding the caixa postal!" }
                return@withLock
            }

            m.logger.info { "Removing caixa postal $playerId from memory!" }
            m.loadedCaixaPostais.remove(playerId, this)

            save()
        }
    }

    private fun save() {
        m.logger.info { "Saving caixa postal $playerId to the database..." }

        transaction(Databases.databaseNetwork) {
            ContaCorreios.insertOrUpdate(ContaCorreios.id) {
                it[id] = playerId
                it[items] = (this@CaixaPostal.items + pendingAddedItems).joinToString(";") { it.toBase64() }
            }
        }
    }
}