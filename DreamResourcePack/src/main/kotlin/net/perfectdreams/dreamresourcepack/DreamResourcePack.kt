package net.perfectdreams.dreamresourcepack

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamresourcepack.commands.DreamRPCommand
import net.perfectdreams.dreamresourcepack.listeners.MoveListener
import org.bukkit.entity.Player
import java.util.*

class DreamResourcePack : KotlinPlugin() {
	val sentToPlayer = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())

	override fun softEnable() {
		super.softEnable()

		registerEvents(MoveListener(this))
		registerCommand(DreamRPCommand(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}