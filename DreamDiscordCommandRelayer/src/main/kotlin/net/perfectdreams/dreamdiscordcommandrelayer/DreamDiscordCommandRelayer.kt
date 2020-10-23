package net.perfectdreams.dreamdiscordcommandrelayer

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.text.DecimalFormat

class DreamDiscordCommandRelayer : KotlinPlugin(), Listener {
	val commandWebhooks = mutableListOf<WebhookClient>()
	var currentWebhookIdx = 0

	override fun softEnable() {
		super.softEnable()

		config.getStringList("command-webhooks").forEach {
			commandWebhooks += WebhookClient.withUrl(it)
		}

		registerEvents(this)
	}

	override fun softDisable() {
		commandWebhooks.forEach { it.close() }
	}

	@EventHandler
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		val currentWebhook = commandWebhooks[currentWebhookIdx % commandWebhooks.size]

		val nf = DecimalFormat("##.##")
		currentWebhook.send(
			WebhookMessageBuilder()
				.setUsername("Gabriela, a amiga dos comandos \uD83D\uDCBB")
				.setAvatarUrl("https://cdn.discordapp.com/attachments/513405772911345664/769319309977583676/gabriela_avatar.png")
				.setContent("[`${e.player.location.world.name}` » `${nf.format(e.player.location.x)}`, `${nf.format(e.player.location.y)}`, `${nf.format(e.player.location.z)}`] **${e.player.name}**: `${e.message}`")
				.build()
		)

		currentWebhookIdx++
	}
}