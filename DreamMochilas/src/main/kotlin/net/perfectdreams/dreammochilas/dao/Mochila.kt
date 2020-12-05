package net.perfectdreams.dreammochilas.dao

import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.fromBase64Inventory
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Mochila(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Mochila>(Mochilas)

    var owner by Mochilas.owner
    var size by Mochilas.size
    var content by Mochilas.content
    var funnyId by Mochilas.funnyId
    var type by Mochilas.type

    fun createMochilaInventory(): Inventory {
        val blahInventory = content.fromBase64Inventory() // Vamos pegar o inventário original

        // E criar ele com o nosso holder personalizado
        val inventory = Bukkit.createInventory(MochilaHolder(this), Math.min(54, size), "§d§lMochila")

        inventory.contents = blahInventory.contents

        return inventory
    }

    fun openMochilaTo(player: Player) {
        val inventory = createMochilaInventory()

        player.openInventory(inventory)
    }

    class MochilaHolder(val mochila: Mochila) : InventoryHolder {
        override fun getInventory(): Inventory {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    fun createItem(): ItemStack {
        var item = ItemStack(Material.CARROT_ON_A_STICK)
                .rename("§rMochila")
                .storeMetadata("isMochila", "true")

        val meta = item.itemMeta
        meta as Damageable
        meta.damage = type ?: 1
        item.itemMeta = meta

        val meta2 = item.itemMeta
        meta2.isUnbreakable = true
        item.itemMeta = meta2

        val playerName = Bukkit.getOfflinePlayer(owner)?.name ?: "???"

        item = item.lore(
                "§7Mochila de §b${playerName}",
                "§7",
                "§6${funnyId}"
        ).storeMetadata("mochilaId", id.value.toString())

        return item
    }
}