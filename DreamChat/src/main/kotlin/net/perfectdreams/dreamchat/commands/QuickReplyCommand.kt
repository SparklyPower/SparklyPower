package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player

class QuickReplyCommand(val m: DreamChat) : AbstractCommand("quicreply", listOf("r")) {
	@Subcommand
	fun root(p0: Player) {
		p0.sendMessage(
				generateCommandInfo(
						"quickreply",
						mapOf(
								"Player" to "O jogador que irá receber a mensagem.",
								"Mensagem" to "A mensagem que você deseja enviar."
						)
				)
		)
	}

	@Subcommand
	fun send(p0: Player, @InjectArgument(ArgumentType.ARGUMENT_LIST) message: String) {
		if (!m.quickReply.containsKey(p0))
			throw ExecutedCommandException("§cVocê não tem ninguém para responder rapidamente!")

		val receiver = m.quickReply[p0]!!

		if (!receiver.isOnline)
			throw ExecutedCommandException("§cInfelizmente o player que estava falando com você não está mais online...")

		if (DreamMini.INSTANCE.queroTrabalhar.contains(receiver)) {
			receiver.sendMessage("§c${p0.displayName}§c tentou te enviar §e${message}§c!")
			throw ExecutedCommandException("§cInfelizmente o player que estava falando com você não está mais online...")
		}

		ChatUtils.sendTell(p0, receiver, message)
	}
}