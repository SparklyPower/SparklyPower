package net.perfectdreams.dreamchat.listeners

import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.Acrobot.ChestShop.Utils.uBlock
import net.perfectdreams.dreamchat.DreamChat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

class SignListener(val m: DreamChat) : Listener {
    @EventHandler
    fun onSignEdit(e: SignChangeEvent) {
        // Don't replace if it is a chest shop sign
        if (ChestShopSign.isValid(e.lines))
            return

        for (i in 0..3) {
            var line = e.getLine(i)

            m.emojis.forEach {
                line = line?.replace(it.chatFormat, "§f" + it.character + "§0") ?: ""
            }

            m.replacers.forEach {
                if (it.value.length == 1) { // Emojis normalmente são apenas um caractere
                    line = line?.replace(it.key, "§f" + it.value + "§0") ?: ""
                } else {
                    line = line?.replace(it.key, it.value) ?: ""
                }
            }

            e.setLine(
                    i,
                    line
            )
        }
    }
}