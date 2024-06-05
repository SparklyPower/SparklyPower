package net.perfectdreams.dreamcorreios.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreamcorreios.tables.ContaCorreios
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.util.*

class CaixaPostal(
    val m: DreamCorreios,
    val playerId: UUID,
    val items: MutableList<ItemStack>
) {
    private val accessHolders = mutableListOf<CaixaPostalAccessHolder>()
    private val accessHolderMutex = Mutex()
    private val pendingAddedItems = mutableListOf<ItemStack>()

    /**
     * This MUST be called within an [DreamCorreios.loadingAndUnloadingCaixaPostalMutex] lock!
     */
    fun createAccess(): CaixaPostalAccessHolder {
        m.logger.info { "Creating an acess holder on caixa postal of $playerId" }
        val accessHolder = CaixaPostalAccessHolder(this)
        accessHolders.add(accessHolder)
        return accessHolder
    }

    /**
     * This MUST be called within an [DreamCorreios.loadingAndUnloadingCaixaPostalMutex] lock!
     */
    fun releaseAccess(accessHolder: CaixaPostalAccessHolder) {
        m.logger.info { "Releasing access holder on caixa postal of $playerId" }
        accessHolders.remove(accessHolder)
        saveIfNotLocked()
    }

    fun addItem(vararg itemStacks: ItemStack) {
        // To avoid replacing added items, we will only add the items when it is time to save the inventory
        // This way we avoid bugs that may arise by "omg I kept the mailbox open while I received a item and now it disappeared!"
        // TODO: Merge items?
        pendingAddedItems.addAll(itemStacks)
    }

    /**
     * This MUST be called within an [DreamCorreios.loadingAndUnloadingCaixaPostalMutex] lock!
     */
    private fun saveIfNotLocked() {
        if (accessHolders.size != 0) {
            m.logger.info { "Not saving caixa postal $playerId because there are still ${accessHolders.size} access holding the caixa postal!" }
            return
        }

        m.logger.info { "Removing caixa postal $playerId from memory!" }
        m.loadedCaixaPostais.remove(playerId, this)

        save()
    }

    private fun save() {
        m.logger.info { "Saving caixa postal $playerId to the database..." }

        transaction(Databases.databaseNetwork) {
            ContaCorreios.upsert(ContaCorreios.id) {
                it[id] = playerId
                it[items] = (this@CaixaPostal.items + pendingAddedItems).joinToString(";") { ItemUtils.serializeItemToBase64(it) }
            }
        }
    }
}