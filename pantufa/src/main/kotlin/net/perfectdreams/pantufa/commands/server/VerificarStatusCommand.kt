package net.perfectdreams.pantufa.commands.server

import net.dv8tion.jda.api.entities.Activity
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object VerificarStatusCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "VerificarStatusCommand", listOf("verificarstatus")) {
		executes {
			val account = pantufa.getDiscordAccountFromUser(this.sender) ?: run {
				reply(
						PantufaReply(
								"Você precisa registrar a sua conta antes de poder receber sonecas pelo status!"
						)
				)
				return@executes
			}

			val customStatus = member?.activities?.firstOrNull { it.type == Activity.ActivityType.CUSTOM_STATUS }

			if (customStatus == null) {
				reply(
						PantufaReply(
								"Você não possui nenhum status!",
								Constants.ERROR
						)
				)
				return@executes
			}

			if (customStatus.name.contains("mc.sparklypower.net") || customStatus.name.contains("discord.gg/sparklypower")) {
				reply(
						PantufaReply(
								"Certinho! Você irá ganhar 15 sonecas por minuto enquanto o seu status estiver ativo! Obrigada por ajudar a divulgar o servidor, seu foof ;3",
								"<:lori_nice:726845783344939028>"
						)
				)
			} else {
				reply(
						PantufaReply(
								"Você precisa colocar `mc.sparklypower.net` ou `discord.gg/sparklypower` no seu status para ganhar os sonecas! Aliás, seja criativo no status! Que tal colocar `Survival 1.16.3: mc.sparklypower.net | Amo esse servidor muito daora e foof!`?",
								Constants.ERROR
						)
				)
			}
		}
	}
}