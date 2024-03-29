package net.perfectdreams.dreammini.commands
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.SubcommandPermission
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class OpenEcCommand(val m: DreamMini) : SparklyCommand(arrayOf("openec", "openenderchest", "ec"), permission = "dreammini.enderchest") {
	@Subcommand(["lookup"])
	fun root(sender: Player, playerName: String? = null) {
		if (!sender.hasPermission("dreammini.seechest")) {
			sender.sendMessage(withoutPermission)
			return
		}

		val player = Bukkit.getPlayer(playerName ?: return) ?: run {
			sender.sendMessage("§c$playerName está offline ou não existe!")
			return
		}

		sender.openInventory(player.getEnderChest())
	
	}

	@Subcommand
	fun root(sender: Player){
		sender.sendMessage("§a§lEnderChest aberta!")
		sender.openInventory(sender.getEnderChest())
	}
}
