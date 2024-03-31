package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class IgnoreCommand(val m: DreamChat) : SparklyCommand(arrayOf("ignore", "ignorar")) {
	@Subcommand
	fun root(p0: Player) {
		p0.sendMessage(generateCommandInfo("ignorar", mapOf("nickname" to "Nome do usuário que você deseja silenciar ou remover o silêncio")))
	}

	@Subcommand
	fun ignore(p0: Player, name: String) {
		val onlinePlayer = Bukkit.getPlayer(name)

		val ignoreList = m.userData.getStringList("ignore.${p0.uniqueId}")

		if (onlinePlayer != null && !DreamVanishAPI.isQueroTrabalhar(onlinePlayer)) {
			if (onlinePlayer.hasPermission("sparklypower.soustaff")) {
				p0.sendMessage("§cVocê não pode ignorar players que são da equipe!")
				onlinePlayer.sendMessage("§cPlayer ${p0.name} tentou te ignorar!")
				return
			}

			if (ignoreList.contains(onlinePlayer.uniqueId.toString())) {
				ignoreList.remove(onlinePlayer.uniqueId.toString())
				p0.sendMessage("§aVocê agora não está mais ignorando ${onlinePlayer.name}!")
			} else {
				ignoreList.add(onlinePlayer.uniqueId.toString())
				p0.sendMessage("§aVocê agora está ignorando ${onlinePlayer.name}!")
			}
			m.userData["ignore.${p0.uniqueId}"] = ignoreList
			scheduler().schedule(DreamChat.INSTANCE, SynchronizationContext.ASYNC) {
				m.userData.save(m.dataYaml)
			}
		} else {
			p0.sendMessage("§cO player que você deseja ignorar não está online ou está offline!")
		}
	}
}