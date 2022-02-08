package net.perfectdreams.dreamvote.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamvote.DreamVote
import net.perfectdreams.dreamvote.tables.Votes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.*

class TagListener(val m: DreamVote) : Listener {
	@EventHandler
	fun onApplyTag(e: ApplyPlayerTagsEvent) {
		if (e.player.name == m.lastVoter) {
			e.tags.add(
					PlayerTag(
							"§c§lV",
							"§c§lÚltimo Votador",
							listOf(
									"§r§b${m.lastVoter}§r§7 ajudou o §4§lSparkly§b§lPower§r§7 a crescer votando!",
									"",
									"§7Que tal ajudar também? :3 §6/votar"
							),
						"/votar",
						true
					)
			)
		}

		val epochMillisAtTheBeginningOfTheMonth = ZonedDateTime
			.now(ZoneId.of("America/Sao_Paulo"))
			.withDayOfMonth(1)
			.withHour(0)
			.withMinute(0)
			.withSecond(0)
			.withNano(0)
			.toInstant()
			.toEpochMilli()

		val isATopVoterThisMonth = transaction(Databases.databaseNetwork) {
			// We do this because there may be multiple users that have the same vote count this month, so we will check the top user vote count and THEN
			// check if the current player has that much votes
			val sumPlayer = Votes.player.count()
			val topVotesCount = Votes.slice(Votes.player, sumPlayer)
				.select { Votes.votedAt greaterEq epochMillisAtTheBeginningOfTheMonth }
				.groupBy(Votes.player)
				.orderBy(sumPlayer, SortOrder.DESC)
				.limit(1)
				.firstOrNull()
				?.getOrNull(sumPlayer)

			if (topVotesCount != null) {
				Votes.slice(Votes.player)
					.select { Votes.votedAt greaterEq epochMillisAtTheBeginningOfTheMonth and (Votes.player eq e.player.uniqueId) }
					.count() == topVotesCount
			} else {
				false
			}
		}

		if (isATopVoterThisMonth) {
			e.tags.add(
				PlayerTag(
					"§c§lTVM",
					"§c§lTop Votador Mensal",
					listOf(
						"§r§b${e.player.name}§r§7 é o top votador deste Mês!",
						"",
						"§7Que tal ajudar também? :3 §6/votar"
					),
					"/votar",
					true
				)
			)
		}

		val topVoter = transaction(Databases.databaseNetwork) {
			val sumPlayer = Votes.player.count()
			Votes.slice(Votes.player, sumPlayer)
				.selectAll()
				.groupBy(Votes.player)
				.orderBy(sumPlayer, SortOrder.DESC)
				.limit(1)
				.firstOrNull()
		}

		if (topVoter != null && topVoter[Votes.player] == e.player.uniqueId) {
			e.tags.add(
				PlayerTag(
					"§c§lTVT",
					"§c§lTop Votador Geral",
					listOf(
						"§r§b${e.player.name}§r§7 é o top votador de todos os tempos!",
						"",
						"§7Que tal ajudar também? :3 §6/votar"
					),
					"/votar",
					true
				)
			)
		}
	}
}