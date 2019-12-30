package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class TpaNegarCommand(val m: DreamMini) : SparklyCommand(arrayOf("tpnegar", "tpanegar", "tpdeny", "tpadeny"), permission = "dreammini.tpnegar") {

	@Subcommand
	fun root(sender: Player) {
		val tpaRequest = m.tpaManager.requests.firstOrNull { it.requestee == sender }
		val tpaHereRequest = m.tpaManager.hereRequests.firstOrNull { it.requestee == sender }

		if (tpaRequest == null || tpaHereRequest == null) {
			sender.sendMessage("§cVocê não tem nenhum pedido de teletransporte pendente!")
			return
		}

		val requester = tpaRequest?.requester ?: tpaHereRequest.requester
		sender.sendMessage("§aVocê rejeitou o pedido de teletransporte de §b${requester.displayName}§a!")
		requester.sendMessage("§b${sender.displayName}§c rejeitou o seu pedido de teletransporte!")
		return
	}
}