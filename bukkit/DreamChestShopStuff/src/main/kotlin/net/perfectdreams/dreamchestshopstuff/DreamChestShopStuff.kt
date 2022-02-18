package net.perfectdreams.dreamchestshopstuff

import net.perfectdreams.dreamchestshopstuff.listeners.ColorizeShopSignsListener
import net.perfectdreams.dreamchestshopstuff.listeners.ShopListener
import net.perfectdreams.dreamchestshopstuff.listeners.ShopParticlesListener
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.Listener

class DreamChestShopStuff : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(ShopListener())
		registerEvents(ColorizeShopSignsListener(this))
		registerEvents(ShopParticlesListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}