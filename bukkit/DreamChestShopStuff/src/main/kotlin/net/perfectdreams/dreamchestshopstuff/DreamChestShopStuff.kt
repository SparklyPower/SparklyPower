package net.perfectdreams.dreamchestshopstuff

import club.minnced.discord.webhook.WebhookClient
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.perfectdreams.dreamchestshopstuff.listeners.ColorizeShopSignsListener
import net.perfectdreams.dreamchestshopstuff.listeners.EconomyTransactionsListener
import net.perfectdreams.dreamchestshopstuff.listeners.ShopListener
import net.perfectdreams.dreamchestshopstuff.listeners.ShopParticlesListener
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.Listener

class DreamChestShopStuff : KotlinPlugin(), Listener {
	override fun softEnable() {
		val config = Yaml.default.decodeFromString<DreamChestShopStuffConfig>(config.saveToString())

		super.softEnable()

		registerEvents(ShopListener(WebhookClient.withUrl(config.suspiciousChestShopWebhookUrl)))
		registerEvents(ColorizeShopSignsListener(this))
		registerEvents(ShopParticlesListener(this))
		registerEvents(EconomyTransactionsListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}