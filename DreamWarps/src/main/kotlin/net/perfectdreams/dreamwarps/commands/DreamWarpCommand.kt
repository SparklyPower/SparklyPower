package net.perfectdreams.dreamwarps.commands

import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamwarps.DreamWarps
import net.perfectdreams.dreamwarps.utils.Warp
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DreamWarpCommand(val m: DreamWarps) : AbstractCommand("dreamwarp", listOf("dreamwarps"), "dreamwarps.setup") {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage("§e/dreamwarps set name")
		player.sendMessage("§e/dreamwarps delete name")
		player.sendMessage("§e/dreamwarps priority name priority")
		player.sendMessage("§e/dreamwarps fancy_name name fancyname")
		player.sendMessage("§e/dreamwarps material name material")
		player.sendMessage("§e/dreamwarps display name true/false")
		player.sendMessage("§e/dreamwarps icon name url")
		player.sendMessage("§e/dreamwarps description name description\\nwow")
		player.sendMessage("§e/dreamwarps reload")
	}

	@Subcommand(["set"])
	fun set(player: Player, name: String) {
		val warp = m.warps.firstOrNull { it.name.equals(name, true) } ?: Warp(name, player.location)
		warp.location = player.location
		m.warps.remove(warp)
		m.warps.add(warp)
		m.saveWarps()
		m.updateChunkTickets()
		player.sendMessage("§aWarp ${name} criada com sucesso!")
		player.sendMessage("§7Permission node: §edreamwarps.warp.$name")
	}

	@Subcommand(["delete"])
	fun delete(player: Player, name: String) {
		val warp = getWarp(name)

		m.warps.remove(warp)
		m.saveWarps()
		m.updateChunkTickets()
		player.sendMessage("§aWarp $name deletada com sucesso!")
	}

	@Subcommand(["priority"])
	fun priority(player: Player, name: String, idx: Int) {
		val warp = getWarp(name)

		warp.priority = idx
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["fancy_name"])
	fun fancyName(player: Player, name: String, @InjectArgument(ArgumentType.ARGUMENT_LIST) fancyName: String) {
		val warp = getWarp(name)

		warp.fancyName = fancyName.translateColorCodes()
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["description"])
	fun description(player: Player, name: String, @InjectArgument(ArgumentType.ARGUMENT_LIST) fancyName: String) {
		val warp = getWarp(name)

		warp.fancyName = fancyName.translateColorCodes()
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["material"])
	fun material(player: Player, name: String, materialName: String) {
		val warp = getWarp(name)

		warp.material = Material.valueOf(materialName)
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["display"])
	fun display(player: Player, name: String, enabled: String) {
		val warp = getWarp(name)

		warp.display = enabled == "true"
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["url"])
	fun url(player: Player, name: String, url: String) {
		val warp = getWarp(name)

		warp.icon = url
		m.saveWarps()
		player.sendMessage("§aWarp ${name} editada com sucesso!")
	}

	@Subcommand(["reload"])
	fun reload(sender: CommandSender) {
		m.loadConfig()
		m.loadWarpsMenu()
		m.updateChunkTickets()

		sender.sendMessage("§aConfiguração recarregada!")
	}

	fun getWarp(name: String) = m.warps.firstOrNull { it.name.equals(name, true) } ?: throw ExecutedCommandException("§cNão existe nenhuma warp chamada ${name}!")
}