package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.blacklistedTeleport
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import net.perfectdreams.dreammini.utils.TpaHereRequest
import org.bukkit.entity.Player

class TpaAquiCommand(val m: DreamMini) : SparklyCommand(arrayOf("tpaaqui", "tpahere"), permission = "dreammini.tpahere") {
	@Subcommand
	fun root(sender: Player) {
		sender.sendMessage(
				generateCommandInfo("tpaaqui <player>")
		)
	}

	@Subcommand
	fun request(sender: Player, @InjectArgument(ArgumentType.PLAYER) requestee: Player?) {
		if (requestee == null) {
			sender.sendMessage("§cPlayer não existe ou está offline!")
			return
		}

		if (sender == requestee) {
			sender.sendMessage("§cVocê não pode enviar um pedido de teletransporte para você mesmo, bobinho!")
			return
		}

		val currentRequest = m.tpaManager.hereRequests.firstOrNull { it.playerThatRequestedTheTeleport == sender }
		if (currentRequest?.playerThatWillBeTeleported == requestee) {
			sender.sendMessage("§cVocê já enviou um pedido para §b${requestee.displayName}§3!")
			return
		}

		if (requestee.location.blacklistedTeleport) {
			sender.sendMessage("§cNossos sistemas de localização não permitem que você teletransporte §b${requestee.displayName}§c para aonde você está!")
			return
		}

		val currentRequestsToAnotherUser = m.tpaManager.hereRequests.filter { it.playerThatWillBeTeleported == requestee }
		m.tpaManager.hereRequests.removeAll(currentRequestsToAnotherUser)

		requestee.sendMessage("§b${sender.displayName}§3 enviou um pedido para você se teletransportar até ele!")
		requestee.sendMessage("§3Para aceitar o pedido, use §6/tpaceitar")
		requestee.sendMessage("§3Para negar o pedido, use §6/tpnegar")
		requestee.sendMessage("§f")
		requestee.sendMessage("§7§lDica:")
		requestee.sendMessage("§8• §7Não aceite pedidos de players que você não conhece!")
		sender.sendMessage("§aPedido de teletransporte enviado para §b" + requestee.displayName + "§a com sucesso!")

		m.tpaManager.hereRequests.remove(currentRequest)
		m.tpaManager.hereRequests.add(TpaHereRequest(sender, requestee))
	}
}