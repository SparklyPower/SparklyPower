package net.perfectdreams.dreammochilas.dao

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.fromBase64Inventory
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreammochilas.tables.Mochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import net.perfectdreams.dreammochilas.utils.MochilaWrapper
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level

class Mochila(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Mochila>(Mochilas)

    var owner by Mochilas.owner
    var size by Mochilas.size
    var content by Mochilas.content
    var funnyId by Mochilas.funnyId
    var type by Mochilas.type

    fun createItem(): ItemStack {
        val playerName = Bukkit.getOfflinePlayer(owner).name ?: "???"

        val item = ItemStack(Material.CARROT_ON_A_STICK)
            .rename("§rMochila")
            .lore(
                "§7Mochila de §b${playerName}",
                "§7",
                "§6${funnyId}"
            )
            .meta<ItemMeta> {
                persistentDataContainer.set(
                    MochilaUtils.IS_MOCHILA_KEY,
                    PersistentDataType.BYTE,
                    1
                )

                persistentDataContainer.set(
                    MochilaUtils.MOCHILA_ID_KEY,
                    PersistentDataType.LONG,
                    id.value
                )
            }

        val meta = item.itemMeta
        meta as Damageable
        meta.damage = type ?: 1
        item.itemMeta = meta

        val meta2 = item.itemMeta
        meta2.isUnbreakable = true
        item.itemMeta = meta2

        return item
    }
}