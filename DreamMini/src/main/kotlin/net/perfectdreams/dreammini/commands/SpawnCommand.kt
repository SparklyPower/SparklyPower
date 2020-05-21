package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.SubcommandPermission
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnCommand(val m: DreamMini) : SparklyCommand(arrayOf("spawn")) {

	@Subcommand
	fun spawn(sender: Player) {
		sender.teleportToServerSpawn()
	}

	@Subcommand(["teleport"])
	@SubcommandPermission("dreammini.spawn.move")
	fun spawn(sender: CommandSender, @InjectArgument(ArgumentType.PLAYER) player: Player?) {
		if (player == null)
			throw ExecutedCommandException("§cPlayer está offline!")

		player.teleportToServerSpawn()

		sender.sendMessage("§6§lTeleportado!")
	}
}