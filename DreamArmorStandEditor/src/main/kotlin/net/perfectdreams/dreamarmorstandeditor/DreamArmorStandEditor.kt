package net.perfectdreams.dreamarmorstandeditor

import net.perfectdreams.dreamarmorstandeditor.commands.ArmorStandEditorCommand
import net.perfectdreams.dreamarmorstandeditor.listeners.InteractListener
import net.perfectdreams.dreamarmorstandeditor.utils.ArmorStandEditorType
import net.perfectdreams.dreamarmorstandeditor.utils.EditType
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Listener

class DreamArmorStandEditor : KotlinPlugin(), Listener {
	val isRotating = mutableMapOf<Player, EditType>()

	override fun softEnable() {
		super.softEnable()

		registerCommand(ArmorStandEditorCommand)
		registerEvents(InteractListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}