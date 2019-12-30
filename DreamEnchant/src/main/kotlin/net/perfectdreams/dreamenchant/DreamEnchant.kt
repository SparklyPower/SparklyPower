package net.perfectdreams.dreamenchant

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamenchant.listeners.BlockListener

class DreamEnchant : KotlinPlugin() {
	override fun softEnable() {
		super.softEnable()

		registerEvents(BlockListener(this))
	}
}