package net.perfectdreams.dreamxizum.dao

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.fromBase64ItemList
import net.perfectdreams.dreamcore.utils.toBase64
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.tables.Kits
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Kit(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Kit>(Kits) {
        fun fetchAll(uuid: UUID) = transaction(Databases.databaseNetwork) {
            find { Kits.uuid eq uuid }.sortedBy { it.slot }
        }.toMutableList()

        fun createKit(uuid: UUID, options: BattleOptions, slot: Int) = transaction(Databases.databaseNetwork) {
            Kit.new {
                this.uuid = uuid
                this.slot = slot
                this.hash = options.hashCode()
                this.items = options.items.toTypedArray().toBase64()
                this.armor = options.armor.toTypedArray().toBase64()
            }
        }
    }

    var uuid by Kits.uuid
    var hash by Kits.hash
    var name by Kits.name
    var slot by Kits.slot
    var items by Kits.items
    var armor by Kits.armor

    fun deleteKit() = transaction(Databases.databaseNetwork) { delete() }
    fun rename(name: String) = transaction(Databases.databaseNetwork) { this@Kit.name = name }
    fun buildItems() = items.fromBase64ItemList().filterNotNullTo(mutableListOf())
    fun buildArmor() = armor.fromBase64ItemList().filterNotNullTo(mutableSetOf())
}