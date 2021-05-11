package net.perfectdreams.dreamchestshopstuff

import net.perfectdreams.dreamchestshopstuff.listeners.ShopListener
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.Listener

class DreamChestShopStuff : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(ShopListener())
	}

	override fun softDisable() {
		super.softDisable()
	}
}