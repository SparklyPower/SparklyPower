package net.perfectdreams.dreamxizum.commands

import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.utils.ArenaXizum
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DreamXizumCommand(val m: DreamXizum) : AbstractCommand("dreamxizum", permission = "dreamxizum.setup") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§e/dreamxizum create name")
		sender.sendMessage("§e/dreamxizum delete name")
		sender.sendMessage("§e/dreamxizum clean name")
		sender.sendMessage("§e/dreamxizum pos1 name")
		sender.sendMessage("§e/dreamxizum pos2 name")
		sender.sendMessage("§e/dreamxizum ready name true/false")
	}

	/* @Subcommand(["create"])
	fun create(player: CommandSender, name: String) {
		m.arenas.add(ArenaXizum(name))
		player.sendMessage("§aArena §e${name}§a criada com sucesso!")
		m.saveArenas()
	}

	@Subcommand(["delete"])
	fun delete(sender: CommandSender, name: String) {
		val arena = getArena(name)

		m.arenas.remove(arena)
		sender.sendMessage("§aArena §e${name}§a deletada com sucesso!")
		m.saveArenas()
	}

	@Subcommand(["clean"])
	fun clean(sender: CommandSender, name: String) {
		val arena = getArena(name)

		arena.player1 = null
		arena.player2 = null
		sender.sendMessage("§aArena §e${name}§a foi limpa com sucesso!")
	}

	@Subcommand(["pos1"])
	fun pos1(player: Player, name: String) {
		val arena = getArena(name)

		arena.location1 = player.location
		player.sendMessage("§aLocalização 1 da arena §e${name}§a alterada com sucesso!")
		m.saveArenas()
	}

	@Subcommand(["pos2"])
	fun pos2(player: Player, name: String) {
		val arena = getArena(name)

		arena.location2 = player.location
		player.sendMessage("§aLocalização 2 da arena §e${name}§a alterada com sucesso!")
		m.saveArenas()
	}

	@Subcommand(["ready"])
	fun ready(player: CommandSender, name: String, value: String) {
		val arena = getArena(name)

		arena.isReady = value == "true"
		player.sendMessage("§aArena §e${name}§a foi alterada com sucesso!")
		m.saveArenas()
	}

	fun getArena(name: String): ArenaXizum = m.getArenaByName(name) ?: throw ExecutedCommandException("§cNão existe nenhuma arena com o nome §e${name}§c!") */
}