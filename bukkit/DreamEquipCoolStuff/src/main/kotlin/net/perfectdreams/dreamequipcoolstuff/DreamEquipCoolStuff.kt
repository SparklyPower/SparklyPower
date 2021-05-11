package net.perfectdreams.dreamequipcoolstuff

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamequipcoolstuff.listeners.InventoryListener
import org.bukkit.event.Listener

class DreamEquipCoolStuff : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(InventoryListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}