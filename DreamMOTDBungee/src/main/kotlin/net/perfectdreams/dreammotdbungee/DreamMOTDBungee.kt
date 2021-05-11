package net.perfectdreams.dreammotdbungee

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ServerPing
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
import java.time.Instant
import java.time.ZoneId
import java.util.*
import javax.imageio.ImageIO

class DreamMOTDBungee : KotlinPlugin(), Listener {
	companion object {
		private val COLOR_LOGO_RED = ChatColor.of(Color(237, 46, 22))
		private val COLOR_LOGO_AQUA = ChatColor.of(Color(1, 235, 247))
	}

	val favicons = mutableListOf<Favicon>()

	override fun onEnable() {
		super.onEnable()
		this.proxy.pluginManager.registerListener(this, this)
		registerCommand(
			DreamMOTDBungeeCommand(this)
		)
		loadFavicons()
	}

	@EventHandler
	fun onProxyPing(e: ProxyPingEvent) {
		val year = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
			.year

		// val version = ProtocolSupportAPI.getProtocolVersion(e.connection.address)
		val online = this.proxy.players.size
		val max = year

		e.response.players.online = online
		e.response.players.max = max

		val top = TextUtils.getCenteredMessage("§a\u266b §6(\uff89\u25d5\u30ee\u25d5)\uff89 §e* :\uff65\uff9f\u2727 ${COLOR_LOGO_RED}§lSparkly${COLOR_LOGO_AQUA}§lPower §e\u2727\uff9f\uff65: *§6\u30fd(\u25d5\u30ee\u25d5\u30fd) §a\u266b", 128)
		// The colored part is "este servidor é incrível!
		val bottom = TextUtils.getCenteredMessage("§5§l»§d§l» §fModéstia à parte, §x§f§f§8§0§8§0e§x§f§f§9§f§8§0s§x§f§f§b§f§8§0t§x§f§f§d§f§8§0e§x§f§f§f§f§8§0 §x§d§f§f§f§8§0s§x§b§f§f§f§8§0e§x§9§f§f§f§8§0r§x§8§0§f§f§8§0v§x§8§0§f§f§9§fi§x§8§0§f§f§b§fd§x§8§0§f§f§d§fo§x§8§0§f§f§f§fr§x§8§0§d§f§f§f §x§8§0§b§f§f§fé§x§8§0§9§f§f§f §x§8§0§8§0§f§fi§x§9§f§8§0§f§fn§x§b§f§8§0§f§fc§x§d§f§8§0§f§fr§x§f§f§8§0§f§fí§x§f§f§8§0§d§fv§x§f§f§8§0§b§fe§x§f§f§8§0§9§fl§f! §d§l«§5§l«", 128)
		// val bottom = TextUtils.getCenteredMessage("§5§l»§d§l» §fQuer coisas da §c§l1.16§f? Então entre! §c^-^ §d§l«§5§l«", 128)
		e.response.descriptionComponent = "$top\n$bottom".toTextComponent()

		e.response.players.sample = arrayOf(
			ServerPing.PlayerInfo(
				"i luv u!",
				UUID.randomUUID()
			)
		)

		if (favicons.isNotEmpty()) {
			e.response.setFavicon(favicons[DreamUtils.random.nextInt(favicons.size)])
		}
	}

	fun loadFavicons() {
		favicons.clear()
		File(dataFolder, "server-icons").listFiles().filter { it.extension == "png" } .forEach {
			this.logger.info("Loading ${it.name}...")
			val icon = ImageIO.read(it)
			favicons.add(Favicon.create(icon))
		}
	}
}