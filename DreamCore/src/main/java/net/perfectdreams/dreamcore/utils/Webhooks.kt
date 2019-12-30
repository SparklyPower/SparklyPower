package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.discord.DiscordWebhook

object Webhooks {
	val PANTUFA: DiscordWebhook by lazy {
		DiscordWebhook(DreamCore.dreamConfig.pantufaWebhook)
	}
	val PANTUFA_INFO: DiscordWebhook by lazy {
		DiscordWebhook(DreamCore.dreamConfig.pantufaInfoWebhook)
	}
	val PANTUFA_ERROR: DiscordWebhook by lazy {
		DiscordWebhook(DreamCore.dreamConfig.pantufaErrorWebhook)
	}
}