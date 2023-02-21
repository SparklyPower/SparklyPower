package net.perfectdreams.dreammobarena

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreammobarena.commands.MobArenaCommand
import net.perfectdreams.dreammobarena.listeners.PlayerListener
import org.bukkit.event.Listener

class DreamMobArena : KotlinPlugin(), Listener {
	val mobArena = MobArena(this)

	override fun softEnable() {
		super.softEnable()

		registerEvents(PlayerListener(this))
		registerCommand(MobArenaCommand(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}