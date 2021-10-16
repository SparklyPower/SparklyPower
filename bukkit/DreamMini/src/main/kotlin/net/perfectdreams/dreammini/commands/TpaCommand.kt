package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreammini.DreamMini
import net.perfectdreams.dreammini.utils.TpaRequest
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

class TpaCommand(val m: DreamMini) : SparklyCommand(arrayOf("tpa", "tpask", "call"), permission = "dreammini.tpa") {
	@Subcommand
	fun root(sender: Player) {
		sender.sendMessage(
				generateCommandInfo("tpa <player>")
		)
	}

	@Subcommand
	fun request(sender: Player, @InjectArgument(ArgumentType.PLAYER) requestee: Player?, @InjectArgument(ArgumentType.ARGUMENTS_AS_STRING) confirm: String? = "") {
		if (requestee == null) {
			sender.sendMessage("§cPlayer não existe ou está offline!")
			return
		}

		if (sender == requestee) {
			sender.sendMessage("§cVocê não pode enviar um pedido de teletransporte para você mesmo, bobinho!")
			return
		}

		val currentRequest = m.tpaManager.requests.firstOrNull { it.requester == sender }
		if (currentRequest?.requestee == requestee) {
			sender.sendMessage("§cVocê já enviou um pedido para §b${requestee.displayName}§3!")
			return
		}

		if (requestee.location.blacklistedTeleport) {
			sender.sendMessage("§cNossos sistemas de localização não permitem que você se teletransporte para aonde §b${requestee.displayName}§c está!")
			return
		}

		if (requestee.location.world.environment == World.Environment.NETHER && confirm != "confirmar") {
			sender.sendMessage("§cEste player está no nether, você tem §c§lcerteza §r§cque deseja se teleportar até ele?")
			sender.sendMessage("§cLembrando que, no nether, você pode perder seus §6itens§c, §lcuidado!")
			sender.sendMessage("§cDigite §6/tpa ${requestee.displayName} confirmar §cse quiser ir mesmo assim.")
			return
		}

		val currentRequestsToAnotherUser = m.tpaManager.requests.filter { it.requestee == requestee }
		m.tpaManager.requests.removeAll(currentRequestsToAnotherUser)

		requestee.sendMessage("§b${sender.displayName}§3 enviou um pedido de teletransporte para você!")
		requestee.sendMessage("§3Para aceitar o pedido, use §6/tpaceitar")
		requestee.sendMessage("§3Para negar o pedido, use §6/tpnegar")
		requestee.sendMessage("§f")
		requestee.sendMessage("§7§lDica:")
		requestee.sendMessage("§8• §7Não aceite pedidos de players que você não conhece!")
		sender.sendMessage("§aPedido de teletransporte enviado para §b" + requestee.displayName + "§a com sucesso!")

		m.tpaManager.requests.remove(currentRequest)
		m.tpaManager.requests.add(TpaRequest(sender, requestee))
	}
}