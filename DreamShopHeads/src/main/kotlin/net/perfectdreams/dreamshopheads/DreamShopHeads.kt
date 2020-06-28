package net.perfectdreams.dreamshopheads

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamshopheads.listeners.InteractListener
import org.bukkit.event.Listener

class DreamShopHeads : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(InteractListener())
	}

	override fun softDisable() {
		super.softDisable()
	}
}