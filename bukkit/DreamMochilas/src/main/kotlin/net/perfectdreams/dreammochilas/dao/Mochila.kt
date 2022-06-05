package net.perfectdreams.dreammochilas.dao

import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreammochilas.tables.Mochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Mochila(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Mochila>(Mochilas)

    var owner by Mochilas.owner
    var size by Mochilas.size
    var content by Mochilas.content
    var funnyId by Mochilas.funnyId
    var type by Mochilas.type
    var version by Mochilas.version

    fun createItem(): ItemStack {
        val playerName = Bukkit.getOfflinePlayer(owner).name ?: "???"

        val item = ItemStack(
            when (version) {
                0 -> Material.CARROT_ON_A_STICK
                else -> Material.PAPER
            }
        ).rename("§rMochila")
            .lore(
                "§7Mochila de §b${playerName}",
                "§7",
                "§6${funnyId}"
            )
            .meta<ItemMeta> {
                if (version == 0) {
                    (this as Damageable).damage = type ?: 1
                } else
                    setCustomModelData(type)

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

        return item
    }
}