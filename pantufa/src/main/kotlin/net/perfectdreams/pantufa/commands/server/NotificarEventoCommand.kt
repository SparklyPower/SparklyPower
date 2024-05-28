package net.perfectdreams.pantufa.commands.server

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.command
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.NotifyPlayersOnline
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object NotificarEventoCommand {
	val eventRoles = mapOf(
		"quiz" to 555184377504464898L,
		"dropparty" to 539979402143072267L,
		"minarecheada" to 538805118384996392L,
		"labirinto" to 539471142772146176L,
		"corrida" to 477979984275701760L,
		"fight" to 798697267312853002L,
		"especial" to 798697126573506571L,
		"mcmmo2x" to 798696876052185119L,
		"enquetes" to 1157110842815418408L
	)

	fun create(pantufa: PantufaBot) = command(pantufa, "NotificarEventoCommand", listOf("notificar evento")) {
		executes {
			val member = this.member ?: return@executes
			val guild = this.message.guild
			if (guild.idLong != 320248230917046282L) {
				reply(
					PantufaReply(
						"Você só pode usar o comando no servidor do SparklyPower! https://discord.gg/sparklypower"
					)
				)
				return@executes
			}

			val eventName = this.args.getOrNull(0)

			if (eventName == null) {
				reply(
					PantufaReply(
						"Escolha um evento para eu te notificar! ${eventRoles.keys.joinToString(", ") { "`$it`" }}"
					)
				)
				return@executes
			} else {
				val eventRoleId = eventRoles[eventName]

				if (eventRoleId == null) {
					reply(
						PantufaReply(
							"Evento inválido! Eventos suportados: ${eventRoles.keys.joinToString(", ") { "`$it`" }}"
						)
					)
					return@executes
				}

				val role = guild.getRoleById(eventRoleId) ?: run {
					reply(
						PantufaReply(
							"Parece que o cargo desse evento não existe mais... Avise para a equipe!"
						)
					)
					return@executes
				}

				if (member.roles.contains(role)) {
					guild.removeRoleFromMember(member, role).await()
					reply(
						PantufaReply(
							"Cargo removido com sucesso! Cansou do evento para remover a notificação dele?"
						)
					)
					return@executes
				} else {
					guild.addRoleToMember(member, role).await()
					reply(
						PantufaReply(
							"Cargo adicionado com sucesso! Quero ver você sempre no evento, hein?"
						)
					)
					return@executes
				}
			}
		}
	}
}