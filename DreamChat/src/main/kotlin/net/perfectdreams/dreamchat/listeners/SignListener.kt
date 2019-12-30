package net.perfectdreams.dreamchat.listeners

import net.perfectdreams.dreamchat.DreamChat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

class SignListener(val m: DreamChat) : Listener {
    @EventHandler
    fun onSignEdit(e: SignChangeEvent) {
        for (i in 0..3) {
            var line = e.getLine(i)

            m.replacers.forEach {
                if (it.value.length == 1) { // Emojis normalmente são apenas um caractere
                    line = line.replace(it.key, "§f" + it.value + "§0")
                } else {
                    line = line.replace(it.key, it.value)
                }
            }

            e.setLine(
                    i,
                    line
            )
        }
    }
}