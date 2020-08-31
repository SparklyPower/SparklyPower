package net.perfectdreams.dreamlagstuffrestrictor

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamlagstuffrestrictor.listeners.BlockLaggyBlocksListener
import net.perfectdreams.dreamlagstuffrestrictor.listeners.BlockMinecartHopperListener
import org.bukkit.event.Listener

class DreamLagStuffRestrictor : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(BlockMinecartHopperListener())
		registerEvents(BlockLaggyBlocksListener())
	}

	override fun softDisable() {
		super.softDisable()
	}
}