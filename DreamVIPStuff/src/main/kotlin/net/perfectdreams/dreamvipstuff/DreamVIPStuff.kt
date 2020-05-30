package net.perfectdreams.dreamvipstuff

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamvipstuff.listeners.PlayerListener
import org.bukkit.event.Listener

class DreamVIPStuff : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§7Bottle§a§lXP§8]"
	}

	override fun softEnable() {
		super.softEnable()

		registerEvents(PlayerListener(this))
	}
}