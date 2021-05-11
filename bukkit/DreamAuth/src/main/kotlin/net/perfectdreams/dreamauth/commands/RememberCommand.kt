package net.perfectdreams.dreamauth.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class RememberCommand(val m: DreamAuth) : SparklyCommand(arrayOf("remember", "lembrar")) {
	@Subcommand
	fun remember(player: Player) {
		m.checkIfNotRegistered(player)
		val authInfo = m.uniqueId2PlayerInfo[player.uniqueId] ?: return
		val remember = authInfo.remember

		val enabled = !remember

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction(Databases.databaseNetwork) {
				authInfo.remember = enabled
			}

			switchContext(SynchronizationContext.SYNC)

			if (enabled) {
				player.sendMessage("§aVocê ativou o sistema de §6\"Tenho preguiça de colocar minha senha toda hora que eu entro no servidor\"§a! Você será logado automaticamente no servidor ao entrar desde que o seu IP não mude.")
				player.sendMessage("");
				player.sendMessage("§4Cuidado: §cSe outras pessoas (que jogam no PerfectDreams) vivem na sua casa e que não são pessoas confiáveis, é recomendado que deixe o sistema de lembrar desativado!")
			} else {
				player.sendMessage("§aVocê desativou o sistema de §6\"Tenho preguiça de colocar minha senha toda hora que eu entro no servidor\"§a!");
			}
		}
	}
}