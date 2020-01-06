package net.perfectdreams.dreamauth.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import org.bukkit.entity.Player

class DreamAuthCommand(val m: DreamAuth) : SparklyCommand(arrayOf("dreamauth"), permission = "dreamauth.setup") {
	@Subcommand(["set_logged_out"])
	fun setLoggedOutSpawn(player: Player) {
		m.config.set("login-location", player.location)
		m.authConfig.loginLocation = player.location

		m.saveConfig()

		player.sendMessage("§aLocalização salva com sucesso!")
	}
}