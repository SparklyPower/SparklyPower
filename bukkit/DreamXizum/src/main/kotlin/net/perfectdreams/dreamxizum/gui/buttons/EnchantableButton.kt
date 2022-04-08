package net.perfectdreams.dreamxizum.gui.buttons

import net.perfectdreams.dreamcore.utils.stripColors
import net.perfectdreams.dreamxizum.config.NPCModel
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantableButton(model: NPCModel, maxLevel: Int, callback: (Player, Int) -> Unit) : Button(model) {
    private var rawLevel = 0

    init {
        sound = null
        enableCooldown = false

        super.callback = { player ->
            rawLevel++

            val level = rawLevel % (maxLevel + 1)
            val color = (ChatColor.COLOR_CHAR + if (level > 0) "a" else "c") + ChatColor.BOLD
            val text = color + "Nível $level"

            npc.lines = npc.lines.mapTo(mutableListOf()) { color + it.stripColors() }.apply { set(lastIndex, text) }
            npc.equipment = npc.equipment.mapTo(mutableSetOf()) {
                it.apply {
                    if (level > 0) addEnchantment(Enchantment.MENDING, 1)
                    else removeEnchantment(Enchantment.MENDING)
                }
            }

            callback.invoke(player, level)
            player.playSound(player.location, if (level > 0) Sound.ENTITY_EXPERIENCE_ORB_PICKUP else Sound.BLOCK_ANVIL_LAND, 10F, 1F)
        }
    }
}