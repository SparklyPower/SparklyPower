package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Material
import org.bukkit.entity.Player

class LoreCommand(val m: DreamMini) : SparklyCommand(arrayOf("lore", "changedescription"), permission = "dreammini.lore"){

	@Subcommand
	fun lore(sender: Player, name: Array<String>){
		val description = name.joinToString(" ").translateColorCodes()

		val item = sender.inventory.itemInMainHand
		val type = item?.type

		if(type != Material.AIR){
			val meta = item.itemMeta

			val lore = description.split("\\n")
			meta.lore = lore
			item.itemMeta = meta

			sender.sendMessage("§aAgora a descrição do item é...")

			for(entry in lore){
				sender.sendMessage("§5§o$entry")
			}
		}else{
			sender.sendMessage("§cSegure um item na sua mão antes de usar!")
		}
	}

	@Subcommand(["reset"])
	fun reset(sender: Player){
		val item = sender.inventory.itemInMainHand
		val type = item?.type

		if(type != Material.AIR){
			val meta = item.itemMeta

			meta.lore = null
			item.itemMeta = meta
		}
	}
}