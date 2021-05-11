package net.perfectdreams.dreambedrockbypass

import fr.neatmonster.nocheatplus.checks.CheckType
import fr.neatmonster.nocheatplus.hooks.NCPHookManager
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import org.bukkit.event.Listener

class DreamBedrockBypass : KotlinPlugin(), Listener {
	val ncpHook = Hook()

	override fun softEnable() {
		super.softEnable()

		NCPHookManager.addHook(CheckType.ALL, ncpHook)
	}

	override fun softDisable() {
		super.softDisable()

		NCPHookManager.removeHook(ncpHook)
	}
}