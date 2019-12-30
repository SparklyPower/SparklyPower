package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class TopCommand(val m: DreamMini) : SparklyCommand(arrayOf("top", "topo", "subir"), permission = "dreammini.top"){

	@Subcommand
	fun root(sender: Player){
		sender.teleport(sender.location.world.getHighestBlockAt(sender.location).location)
		sender.sendMessage("§b彡ﾟ◉ω◉  )つー☆* §aVocê se teleportou para o topo §b*☆ーつ(  ◉ω◉ﾟ彡")
	}
}