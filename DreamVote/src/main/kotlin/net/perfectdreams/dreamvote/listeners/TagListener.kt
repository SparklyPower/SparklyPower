package net.perfectdreams.dreamvote.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamvote.DreamVote
import net.perfectdreams.dreamvote.tables.Votes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction

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

		val topVoter = transaction(Databases.databaseNetwork) {
			val sumPlayer = Votes.player.count()
			Votes.slice(Votes.player, sumPlayer)
				.selectAll()
				.groupBy(Votes.player)
				.limit(1)
				.firstOrNull()
		}

		if (topVoter != null && topVoter[Votes.player] == e.player.uniqueId) {
			e.tags.add(
				PlayerTag(
					"§c§lT",
					"§c§lTop Votador",
					listOf(
						"§r§b${m.lastVoter}§r§7 é o top votador!",
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