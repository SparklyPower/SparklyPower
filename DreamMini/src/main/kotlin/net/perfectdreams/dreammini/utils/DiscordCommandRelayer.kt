package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamcore.utils.discord.DiscordWebhook
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.text.DecimalFormat

class DiscordCommandRelayer(val m: DreamMini) : Listener {
	companion object {
		lateinit var COMMAND_WEBHOOK: DiscordWebhook
	}

	init {
		COMMAND_WEBHOOK = DiscordWebhook(m.config.getString("command-relay.webhook-url"))
	}

	@EventHandler
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		val nf = DecimalFormat("##.##")
		COMMAND_WEBHOOK.send(
				DiscordMessage(
						content = "[`${e.player.location.world.name}` » `${nf.format(e.player.location.x)}`, `${nf.format(e.player.location.y)}`, `${nf.format(e.player.location.z)}`] **${e.player.name}**: `${e.message}`"
				)
		)
	}
}