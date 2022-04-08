package net.perfectdreams.dreamxizum.gui.buttons

import net.perfectdreams.dreamcore.utils.DreamNPC
import net.perfectdreams.dreamcore.utils.createNPC
import net.perfectdreams.dreamxizum.config.NPCModel
import org.bukkit.Sound
import org.bukkit.entity.Player

open class Button(val model: NPCModel, var callback: (Player) -> Unit = {}) {
    internal lateinit var npc: DreamNPC
    private var lastClick = 0L

    internal var sound: Sound? = Sound.UI_BUTTON_CLICK
    internal var name = model.displayName
    internal var enableCooldown = true

    fun register(): Button {
        npc = createNPC(name, model.coordinates.toBukkitLocation()) {
            skin {
                texture = model.skin.texture
                signature = model.skin.signature
            }

            onClick { player ->
                if (enableCooldown && System.currentTimeMillis() - lastClick < 500) return@onClick
                sound?.let { player.playSound(player.location, it, 10F, 1F) }
                lastClick = System.currentTimeMillis()
                callback.invoke(player)
            }
        }
        return this
    }
}