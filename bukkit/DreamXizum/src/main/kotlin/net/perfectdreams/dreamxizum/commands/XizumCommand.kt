package net.perfectdreams.dreamxizum.commands

import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.utils.XizumInventoryHolder
import net.perfectdreams.dreamxizum.utils.XizumRequest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class XizumCommand(val m: DreamXizum) : AbstractCommand("xizum", listOf("x1", "1v1")) {
	@Subcommand
	fun root(player: Player) {
		// Uncomment this when the "request player" function works
		/* val inventory = Bukkit.createInventory(XizumInventoryHolder(), 9)

		val joinQueue = ItemStack(Material.DIAMOND)
				.rename("§a§lEntrar na Fila")
				.storeMetadata(DreamXizum.XIZUM_DATA_KEY, "joinQueue")

		val requestPlayer = ItemStack(Material.MUSIC_DISC_11)
				.rename("§a§lChamar um amigo")
				.storeMetadata(DreamXizum.XIZUM_DATA_KEY, "invitePlayer")

		inventory.setItem(0, joinQueue)
		inventory.setItem(1, requestPlayer)

		player.openInventory(inventory) */
		player.performCommand("xizum fila")
	}

	@Subcommand(["fila"])
	fun file(p0: Player) {
		val alreadyInXizum = m.arenas.any { it.player1 == p0 || it.player2 == p0 }
		if (alreadyInXizum) {
			p0.sendMessage("${DreamXizum.PREFIX} §cVocê já está em uma partida de Xizum!")
			return
		}

		if (m.queue.contains(p0)) {
			m.queue.remove(p0)
			p0.sendMessage("${DreamXizum.PREFIX} §cVocê saiu da fila do Xizum!")
			return
		}
		m.addToQueue(p0)
		if (m.queue.contains(p0)) {
			p0.sendMessage("${DreamXizum.PREFIX} §aVocê entrou na fila do Xizum!")
		}
	}

	@Subcommand(["aceitar"])
	fun aceitar(p0: Player) {
		val xizumRequest = m.requests.firstOrNull { it.requestee == p0 }

		if (xizumRequest == null) {
			p0.sendMessage("${DreamXizum.PREFIX} §cVocê não tem nenhum pedido de Xizum!")
			return
		}

		val alreadyInXizum = m.arenas.any { it.player1 == p0 || it.player2 == p0 }
		if (alreadyInXizum) {
			p0.sendMessage("${DreamXizum.PREFIX} §cVocê já está em uma partida de Xizum!")
			return
		}

		m.queue.remove(xizumRequest.requester)
		m.queue.remove(xizumRequest.requestee)
		m.requestQueue.removeAll { it.player1 == xizumRequest.requester || it.player2 == xizumRequest.requester }
		m.requestQueue.removeAll { it.player1 == xizumRequest.requestee || it.player2 == xizumRequest.requestee }

		m.addToRequestQueue(xizumRequest.requester, xizumRequest.requestee)
	}

	@Subcommand(["convidar"])
	fun convidar(sender: Player, @InjectArgument(ArgumentType.PLAYER) receiver: Player?) {
		if (receiver == null) {
			sender.sendMessage("${DreamXizum.PREFIX} §cJogador não existe ou está offline!")
			return
		}

		if (sender == receiver) {
			sender.sendMessage("${DreamXizum.PREFIX} §cVocê não pode chamar a si mesmo para o Xizum, bobinho.")
			return
		}

		val alreadyInXizum = m.arenas.any { it.player1 == sender || it.player2 == sender }
		if (alreadyInXizum) {
			sender.sendMessage("${DreamXizum.PREFIX} §cVocê já está em uma partida de Xizum!")
			return
		}

		val xizumRequest = XizumRequest(sender, receiver)
		m.requests.add(xizumRequest)

		receiver.sendMessage("${DreamXizum.PREFIX} §b${sender.displayName}§3 te enviou um pedido para Xizum, para aceitar, use §6/xizum aceitar")
		sender.sendMessage("${DreamXizum.PREFIX} §aConvite de Xizum para §b${receiver.displayName}§a foi enviado com sucesso!")
	}
}