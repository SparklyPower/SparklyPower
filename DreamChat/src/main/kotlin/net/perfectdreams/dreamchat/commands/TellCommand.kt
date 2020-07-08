package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.collections.set

class TellCommand(val m: DreamChat) : AbstractCommand("tell", listOf("msg", "pm", "m", "whisper", "w")) {
	@Subcommand
	fun root(p0: CommandSender) {
		if (m.lockedTells.containsKey(p0)) {
			p0.sendMessage("§aSeu chat travado com §b${m.lockedTells[p0]}§a foi desativado")
			m.lockedTells.remove(p0)
			return
		}
		p0.sendMessage(
				generateCommandInfo(
						"tell",
						mapOf(
								"Player" to "O jogador que irá receber a mensagem.",
								"Mensagem" to "A mensagem que você deseja enviar."
						)
				)
		)
	}

	@Subcommand
	fun lock(sender: Player, @InjectArgument(ArgumentType.PLAYER) lock: Player?) {
		if (lock == null)
			throw ExecutedCommandException("§cJogador não existe ou está offline!")

		if (sender == lock)
			throw ExecutedCommandException("§cVocê não pode enviar uma mensagem para você mesmo, bobinh${sender.artigo}!")

		m.lockedTells[sender] = lock.name
		sender.sendMessage("§aSeu chat foi travado com ${lock.artigo} §b${sender.displayName}§a! Agora você pode enviar mensagens no chat e elas irão ir para a caixa privada d${lock.artigo} §b${lock.displayName}§a!")
		sender.sendMessage("§7Para desativar, use §6/tell")
		return
	}

	@Subcommand
	fun send(sender: Player, @InjectArgument(ArgumentType.PLAYER) receiver: Player?, @InjectArgument(ArgumentType.ARGUMENT_LIST) message: String) {
		if (receiver == null)
			throw ExecutedCommandException("§cJogador não existe ou está offline!")

		if (DreamVanishAPI.isQueroTrabalhar(receiver)) {
			receiver.sendMessage("§c${sender.displayName}§c tentou te enviar §e${message}§c!")
			throw ExecutedCommandException("§cJogador não existe ou está offline!")
		}

		if (sender == receiver)
			throw ExecutedCommandException("§cVocê não pode enviar uma mensagem para você mesmo, bobinh${sender.artigo}!")

		ChatUtils.sendTell(sender, receiver, message)
	}
}