package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.discord.DiscordWebhook

object Webhooks {
	val PANTUFA: DiscordWebhook? by lazy {
		DreamCore.dreamConfig.discord?.webhooks?.info?.let {
			DiscordWebhook(it)
		}
	}
	val PANTUFA_INFO: DiscordWebhook? by lazy {
		DreamCore.dreamConfig.discord?.webhooks?.info?.let {
			DiscordWebhook(it)
		}
	}
	val PANTUFA_ERROR: DiscordWebhook? by lazy {
		DreamCore.dreamConfig.discord?.webhooks?.error?.let {
			DiscordWebhook(it)
		}
	}
}