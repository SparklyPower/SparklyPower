package net.perfectdreams.dreamxizum.gui.buttons

import net.perfectdreams.dreamcore.utils.stripColors
import net.perfectdreams.dreamxizum.config.NPCModel
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class ToggleableButton(model: NPCModel, callback: (Player, Boolean) -> Unit) : Button(model) {
    private var toggled = false

    init {
        enableCooldown = false

        super.callback = { player ->
            toggled = !toggled

            val color = (ChatColor.COLOR_CHAR + if (toggled) "a" else "c") + ChatColor.BOLD
            val text = color + if (toggled) "Sim" else "Não"
            npc.lines = npc.lines.mapTo(mutableListOf()) { color + it.stripColors() }.apply { set(lastIndex, text) }

            callback.invoke(player, toggled)
        }
    }
}