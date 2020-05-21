package net.perfectdreams.dreamvanish

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamvanish.commands.QueroTrabalharCommand
import net.perfectdreams.dreamvanish.commands.VanishCommand
import net.perfectdreams.dreamvanish.listeners.PlayerListener
import org.bukkit.Bukkit

class DreamVanish : KotlinPlugin() {
	companion object {
		lateinit var INSTANCE: DreamVanish
	}

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		registerCommand(QueroTrabalharCommand)
		registerCommand(VanishCommand)

		registerEvents(PlayerListener(this))

		schedule {
			while (true) {
				Bukkit.getOnlinePlayers().forEach {
					val isVanished = DreamVanishAPI.isVanished(it)
					val isQueroTrabalhar = DreamVanishAPI.isQueroTrabalhar(it)

					if (isVanished || isQueroTrabalhar)
						it.sendActionBar("§aVocê está invisível e no modo quero trabalhar!")
					if (isVanished)
						it.sendActionBar("§aVocê está invisível! Vanish Poder O2~")
					else if (isQueroTrabalhar)
						it.sendActionBar("§aVocê está no modo quero trabalhar! Apenas trabalho, sem diversão hein grrr")
				}

				waitFor(20)
			}
		}
	}
}