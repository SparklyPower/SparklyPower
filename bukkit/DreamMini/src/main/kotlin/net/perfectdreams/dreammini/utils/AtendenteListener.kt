package net.perfectdreams.dreammini.utils

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.DreamMenu
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.io.File

class AtendenteListener(val m: DreamMini) : Listener {
	lateinit var customAtendenteMenu: Any

	fun loadAtendenteMenu() {
		val menuFile = File(m.dataFolder, "atendente_menu.kts")
		customAtendenteMenu = DreamScriptManager.evaluate<Any>(m, """
			${Imports.IMPORTS}

			class CustomAtendenteMenu {
				fun generateMenu(player: Player) = ${menuFile.readText()}
			}

			CustomAtendenteMenu()
		""".trimIndent())
	}

	@EventHandler
	fun onClick(e: PlayerInteractEntityEvent) {
		if (e.rightClicked.name == "§a§lAtendente" && e.rightClicked.location.isWithinRegion("loja")) {
			val result = customAtendenteMenu::class.java.getMethod("generateMenu", Player::class.java).invoke(customAtendenteMenu, e.player) as DreamMenu
			result.sendTo(e.player)
		}
	}

	class AtendenteMenuCommand(val m: AtendenteListener) : SparklyCommand(arrayOf("atendentemenu"), permission = "dreammini.atendente") {

		@Subcommand
		fun root(sender: CommandSender) {
			sender.sendMessage("§aRecarregando...")
			m.loadAtendenteMenu()
			sender.sendMessage("§aProntinho!")
		}
	}
}