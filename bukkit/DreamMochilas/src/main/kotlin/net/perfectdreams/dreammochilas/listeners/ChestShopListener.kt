package net.perfectdreams.dreammochilas.listeners

import com.Acrobot.ChestShop.Events.ItemParseEvent
import net.perfectdreams.dreamcore.utils.SparklyNamespacedBooleanKey
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.utils.MochilaData
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ChestShopListener : Listener {
    companion object {
        // TODO: Remove this from here, it should be in another plugin
        private val IS_MOVE_SPAWNERS_KEY = SparklyNamespacedBooleanKey("is_move_spawners_tool")
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onClick(e: ItemParseEvent) {
        val cleanItemString = ChatColor.stripColor(e.itemString)!!
        if (cleanItemString == "Moch Aniversário") {
            e.item = DreamMochilas.createMochila(MochilaData.birthday9.random())
        } else {
            if (cleanItemString.startsWith("Mochila ")) {
                val mochilaName = cleanItemString.substringAfter("Mochila ")

                val mochilaData = MochilaData.list.firstOrNull { it.name.startsWith(mochilaName, true) }
                if (mochilaData != null) {
                    e.item = DreamMochilas.createMochila(mochilaData)
                }
            }
        }

        // TODO: Remove this from here, it should be in another plugin
        if (cleanItemString == "Mover Spawners") {
            e.item = ItemStack(Material.GOLDEN_PICKAXE)
                .rename("§6§lPicareta de Mover Spawners")
                .lore("§7Querendo mover spawners para outros lugares?", "§7Então utilize a incrível picareta de mover spawners!", "§7", "§7Cuidado que ela quebra bem rápido!")
                .apply {
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    this.addUnsafeEnchantment(Enchantment.INFINITY, 1)
                }
                .meta<ItemMeta> {
                    persistentDataContainer.set(IS_MOVE_SPAWNERS_KEY, true)
                }
        }
    }
}