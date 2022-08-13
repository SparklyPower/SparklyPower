package net.perfectdreams.dreamresourcegenerator

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamresourcegenerator.commands.DreamResourceResetRegenWorldCommand
import org.bukkit.event.Listener

class DreamResourceGenerator : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()
		registerCommand(DreamResourceResetRegenWorldCommand)
	}

	override fun softDisable() {
		super.softDisable()
	}
}