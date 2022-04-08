package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material

class ChooseWeaponPage(val options: BattleOptions) : AbstractPage() {
    init {
        button(npcs.barehands) {
            Paginator.fetch(it).addAndShowPage(ChooseArmorPage(options))
        }

        button(npcs.swords) {
            Paginator.fetch(it).addAndShowPage(WeaponsPage(listOf(Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD), options))
        }.npc.changeItem(Material.IRON_SWORD)

        button(npcs.axes) {
            Paginator.fetch(it).addAndShowPage(WeaponsPage(listOf(Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE), options))
        }.npc.changeItem(Material.GOLDEN_AXE)
    }

    override fun onReturn() { options.items.removeFirstOrNull() }
}