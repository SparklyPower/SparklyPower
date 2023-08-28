package net.perfectdreams.dreamcore.utils

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import net.perfectdreams.dreamcore.DreamCore

object Webhooks {
	val PANTUFA_NEWS: WebhookClient? by lazy {
		DreamCore.dreamConfig.discord.webhooks.news.let {
			WebhookClientBuilder(it).build()
		}
	}
	val PANTUFA_WARN: WebhookClient? by lazy {
		DreamCore.dreamConfig.discord.webhooks.warn.let {
			WebhookClientBuilder(it).build()
		}
	}
	val PANTUFA_INFO: WebhookClient? by lazy {
		DreamCore.dreamConfig.discord.webhooks.info.let {
			WebhookClientBuilder(it).build()
		}
	}
	val PANTUFA_ERROR: WebhookClient? by lazy {
		DreamCore.dreamConfig.discord.webhooks.error.let {
			WebhookClientBuilder(it).build()
		}
	}
}