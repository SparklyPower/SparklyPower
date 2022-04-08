package net.perfectdreams.dreamxizum.gui.pages.kits

import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.FinalPage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class PluginKitsPage(val options: BattleOptions) : AbstractPage() {
    companion object {
        private val slots = setOf("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS")
        fun getArmor(type: String) = slots.map { Material.getMaterial("${type}_$it")!!.toItemStack() }
    }

    private val kits = listOf(
        mutableListOf(Material.DIAMOND_SWORD.toItemStack()),
        mutableListOf(Material.NETHERITE_AXE.toItemStack().apply { addEnchantment(Enchantment.DAMAGE_ALL, 5) }).apply {
            add(Material.BOW.toItemStack().apply { addEnchantment(Enchantment.ARROW_DAMAGE, 5) })
            add(Material.ARROW.toItemStack(64))
        },
        mutableListOf(Material.WOODEN_SWORD.toItemStack())
    )

    private val armors = listOf(
        getArmor("IRON").map { it.apply { addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4) } },
        getArmor("DIAMOND").map { it.apply { addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4) } }
    )

    init {
        listOf(npcs.pluginKit1, npcs.pluginKit2, npcs.pluginKit3).forEachIndexed { index, model ->
            button(model) {
                if (it.isSneaking) return@button createMenu(9, "§6§lItens do kit:") {
                    kits[index].forEachIndexed { x, item -> slot(x, 0) { this.item = item } }
                    if (index < 2) armors[index].forEachIndexed { x, item -> slot(x + kits[index].size, 0) { this.item = item } }
                }.sendTo(it)

                options.items = kits[index]
                if (index < 2) options.armor = armors[index].toMutableSet()
                Paginator.fetch(it).addAndShowPage(FinalPage(it))
            }
        }
    }

    override fun onReturn() { options.clearItems() }
}