package net.perfectdreams.dreamvote.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamvote.DreamVote
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

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
							)
					)
			)
		}
	}
}