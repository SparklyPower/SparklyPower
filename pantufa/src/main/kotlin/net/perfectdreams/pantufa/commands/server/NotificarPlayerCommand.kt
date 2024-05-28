package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object NotificarPlayerCommand {
	fun create(pantufa: PantufaBot) = command(pantufa, "NotificarPlayerCommand", listOf("notificar player")) {
		executes {
			val playerName = this.args.getOrNull(0) ?: run {
				reply(
						PantufaReply(
								"Cadê o player que você deseja ser notificado nn sei"
						)
				)
				return@executes
			}

			val minecraftUser = pantufa.getMinecraftUserFromUsername(playerName) ?: run {
				reply(
						PantufaReply(
								"O usuário `${playerName.replace("`", "")}` parece não jogar no SparklyPower, tem certeza que colocou o nick correto?"
						)
				)
				return@executes
			}

			val selfAccount = pantufa.getDiscordAccountFromUser(this.sender) ?: run {
				reply(
						PantufaReply(
								"Você precisa registrar a sua conta antes de poder receber notificações!"
						)
				)
				return@executes
			}

			val existingTracker = transaction(Databases.sparklyPower) {
				NotifyPlayersOnline.select { NotifyPlayersOnline.player eq selfAccount.minecraftId and (NotifyPlayersOnline.tracked eq minecraftUser.id.value) }
						.count() != 0L
			}

			if (existingTracker) {
				transaction(Databases.sparklyPower) {
					NotifyPlayersOnline.deleteWhere { NotifyPlayersOnline.player eq selfAccount.minecraftId and (NotifyPlayersOnline.tracked eq minecraftUser.id.value) }
				}

				reply(
						PantufaReply(
								"Você agora vai parar de receber notificações quando `${playerName.replace("`", "")}` entra no SparklyPower!"
						)
				)
			} else {
				transaction(Databases.sparklyPower) {
					NotifyPlayersOnline.insert {
						it[player] = selfAccount.minecraftId
						it[tracked] = minecraftUser.id.value
					}
				}

				reply(
						PantufaReply(
								"Você agora irá receber notificações quando `${playerName.replace("`", "")}` entrar no SparklyPower! A força da amizade sempre prevalece!"
						)
				)
			}
		}
	}
}