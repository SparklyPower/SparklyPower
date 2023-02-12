package net.perfectdreams.dreammotdbungee

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.event.PlayerHandshakeEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamcorebungee.KotlinPlugin
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.TextUtils
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreammotdbungee.commands.DreamMOTDBungeeCommand
import java.awt.Color
import java.io.File
import java.time.*
import java.util.*
import javax.imageio.ImageIO

class DreamMOTDBungee : KotlinPlugin(), Listener {
	companion object {
		private val COLOR_LOGO_RED = ChatColor.of(Color(237, 46, 22))
		private val COLOR_LOGO_AQUA = ChatColor.of(Color(1, 235, 247))
	}

	val favicons = mutableMapOf<String, Favicon>()
	val isMaintenance = File(dataFolder, "maintenance").exists()

	override fun onEnable() {
		super.onEnable()
		this.proxy.pluginManager.registerListener(this, this)
		registerCommand(DreamMOTDBungeeCommand(this))
		loadFavicons()
	}

	@EventHandler
	fun onLogin(e: PreLoginEvent) {
		if (isMaintenance) {
			e.isCancelled = true
			e.setCancelReason("§cSparklyPower está em manutenção! Acompanhe atualizações sobre a manutenção em nosso Discord: https://discord.gg/sparklypower".toTextComponent())
		}
	}

	@EventHandler
	fun onProxyPing(e: ProxyPingEvent) {
		val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
		val currentDayOfTheWeek = now.dayOfWeek
		val year = now.year

		// val version = ProtocolSupportAPI.getProtocolVersion(e.connection.address)
		val online = this.proxy.players.size
		val max = year

		e.response.players.online = online
		e.response.players.max = max

		val top: String
		val bottom: String

		if (isMaintenance) {
			e.response.setFavicon(favicons["pantufa_zz"])

			top = TextUtils.getCenteredMessage("§cSparklyPower está em manutenção!", 128)

			bottom = TextUtils.getCenteredMessage(
				"§cVolte mais tarde!",
				128
			)
		} else {
			top = TextUtils.getCenteredMessage("§a\u266b §6(\uff89\u25d5\u30ee\u25d5)\uff89 §e* :\uff65\uff9f\u2727 ${COLOR_LOGO_RED}§lSparkly${COLOR_LOGO_AQUA}§lPower §e\u2727\uff9f\uff65: *§6\u30fd(\u25d5\u30ee\u25d5\u30fd) §a\u266b", 128)

			bottom = if (currentDayOfTheWeek == DayOfWeek.FRIDAY) {
				e.response.setFavicon(favicons["pantufa_emojo"])

				// The colored part is "este servidor é incrível!
				TextUtils.getCenteredMessage(
					"§5§l»§d§l» §x§d§5§d§6§1§0HOJE É SEXTA CAMBADA! VAMOS ANIMAR!!! §d§l«§5§l«",
					128
				)
			} else {
				e.response.setFavicon(favicons["pantufa_sortros"])

				// The colored part is "este servidor é incrível!
				TextUtils.getCenteredMessage(
					"§5§l»§d§l» §fModéstia à parte, §x§f§f§8§0§8§0e§x§f§f§9§f§8§0s§x§f§f§b§f§8§0t§x§f§f§d§f§8§0e§x§f§f§f§f§8§0 §x§d§f§f§f§8§0s§x§b§f§f§f§8§0e§x§9§f§f§f§8§0r§x§8§0§f§f§8§0v§x§8§0§f§f§9§fi§x§8§0§f§f§b§fd§x§8§0§f§f§d§fo§x§8§0§f§f§f§fr§x§8§0§d§f§f§f §x§8§0§b§f§f§fé§x§8§0§9§f§f§f §x§8§0§8§0§f§fi§x§9§f§8§0§f§fn§x§b§f§8§0§f§fc§x§d§f§8§0§f§fr§x§f§f§8§0§f§fí§x§f§f§8§0§d§fv§x§f§f§8§0§b§fe§x§f§f§8§0§9§fl§f! §d§l«§5§l«",
					128
				)
			}
		}

		// val bottom = TextUtils.getCenteredMessage("§5§l»§d§l» §fQuer coisas da §c§l1.16§f? Então entre! §c^-^ §d§l«§5§l«", 128)
		e.response.descriptionComponent = "$top\n$bottom".toTextComponent()

		e.response.players.sample = arrayOf(
			createPlayerListMessage("§b✦§3§m                    §8[§4§lSparkly§b§lPower§8]§3§m                    §b✦"),
			createPlayerListMessage("§6✧ §fModéstia a parte, este servidor é §a§lincrível§f! §6✧"),
			createPlayerListMessage(""),
			createPlayerListMessage("§b✦ §6$online§e Players Online! §b✦"),
			createPlayerListMessage("§b✦ §eUm §6Survival§e que você §6jamais§e viu antes! §b✦"),
			createPlayerListMessage("§b✦ §eServidor da §6Loritta Morenitta§e! §b✦"),
			createPlayerListMessage("§b✦ §eItens §6personalizados§e! §b✦"),
			createPlayerListMessage("§b✦ §eSem §6Lag§e! (as vezes né) §b✦"),
			createPlayerListMessage("§b✦ §eDesde §62014§e divertindo nossos jogadores! §b✦"),
			createPlayerListMessage("§b✦ §ee §6§lmuito§e mais! §b✦"),
			createPlayerListMessage(""),
			createPlayerListMessage("§5✸ §eentre agora... §5✸"),
			createPlayerListMessage("§d✸ §eporque só falta você! §c✌ §d✸"),
		)
	}

	private fun createPlayerListMessage(text: String) = ServerPing.PlayerInfo(
		TextUtils.getCenteredMessage(
			text,
			128
		),
		UUID.randomUUID()
	)

	fun loadFavicons() {
		favicons.clear()
		File(dataFolder, "server-icons").listFiles().filter { it.extension == "png" } .forEach {
			this.logger.info("Loading ${it.name}...")
			val icon = ImageIO.read(it)
			favicons[it.nameWithoutExtension] = Favicon.create(icon)
		}
	}
}