package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class DreamMiniCommand(val m: DreamMini) : SparklyCommand(arrayOf("dreammini"), permission = "dreammini.setup"){
	@Subcommand
	fun reloadConfig(sender: Player) {
		m.reloadConfig()
		sender.sendMessage("Configuração recarregada com sucesso!")
	}
}