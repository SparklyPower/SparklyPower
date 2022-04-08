package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamcore.utils.DreamNPC
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.CrossbowMeta

class ExtrasPage(val options: BattleOptions, val player: Player) : AbstractPage() {
    companion object {
        private val items = listOf(Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.SHIELD)
        private val sounds = listOf(Sound.ENTITY_ARROW_SHOOT, Sound.ITEM_CROSSBOW_SHOOT, Sound.ITEM_TRIDENT_THROW, Sound.ITEM_SHIELD_BLOCK)
        private val stackedCrossbow = items[1].toItemStack().meta<CrossbowMeta> { setChargedProjectiles(listOf(Material.ARROW.toItemStack())) }
    }

    private val models = with(npcs) { listOf(bow, crossbow, trident, shield) }

    init {
        onReturn()

        models.forEachIndexed { index, model ->
            val item = items[index]
            lateinit var dreamNPC: DreamNPC
            toggleableButton(model) { player, toggled ->
                if (toggled) options.items.add(item.toItemStack()) else options.items.removeAll { it.type == item }
                if (item == Material.CROSSBOW) dreamNPC.changeItem(if (toggled) stackedCrossbow else item.toItemStack())
                else dreamNPC.isRightClicking = toggled
                player.playSound(player.location, sounds[index], 10F, 1F)

                onReturn()
            }.npc.apply {
                dreamNPC = this
                changeItem(item)
            }
        }
    }

    override fun onReturn() { nextPage = Paginator.inferNextPage(player, 4) }

    override fun onBack() = items.forEach { options.items.removeAll { item -> it == item.type } }
}