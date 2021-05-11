package net.perfectdreams.dreamnetworkbans.commands

import com.github.salomonbrys.kotson.fromJson
import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import java.io.File

class YouTuberAssistCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("youtuberassist"), permission = "dreamnetworkbans.youtuberassist") {
	
	@Subcommand
    fun root(sender: CommandSender) {
		sender.sendMessage("§c/youtuberassist add player".toTextComponent())
		sender.sendMessage("§c/youtuberassist remove player".toTextComponent())
		sender.sendMessage("§c/youtuberassist reload".toTextComponent())
	}
	
	@Subcommand(["reload"])
	fun reload(sender: CommandSender) {
		m.youtuberNames = DreamUtils.gson.fromJson(File("youtubers.json").readText())
		
		sender.sendMessage("§aNomes recarregados com sucesso!".toTextComponent())
	}
	
	@Subcommand(["add"])
    fun add(sender: CommandSender, name: String) {
		val file = File("youtubers.json")
		
		m.youtuberNames.add(name.toLowerCase())
		file.writeText(DreamUtils.gson.toJson(m.youtuberNames))
		
		sender.sendMessage("§aAdicionado com sucesso!".toTextComponent())
	}
	
	@Subcommand(["remove"])
    fun remove(sender: CommandSender, name: String) {
		val file = File("youtubers.json")
		
		m.youtuberNames.remove(name.toLowerCase())
		file.writeText(DreamUtils.gson.toJson(m.youtuberNames))
		
		sender.sendMessage("§aRemovido com sucesso!".toTextComponent())
	}
}