package net.perfectdreams.dreamscoreboard

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamscoreboard.commands.EventosCommand
import net.perfectdreams.dreamscoreboard.commands.GlowingColorCommand
import net.perfectdreams.dreamscoreboard.commands.GlowingCommand
import net.perfectdreams.dreamscoreboard.utils.PlayerScoreboard
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DreamScoreboard : KotlinPlugin(), Listener {
	companion object {
		var CURRENT_TICK = 0

		val EMOTES = listOf(
				"锃", // gesso
				"锅", // eu te moido
				"锇", // grand cat
				"锈", // wave
				"锍", // emojo
				"镞", // lori popcorn
				"镝", // lori temmie
				"镾", // tavares
				"閍", // vieirinha
				"閌", // loritta
				"開", // lori feliz
				"閊"  // pepo feliz
		)
		val FORMATTING_REGEX = Regex("§[k-or]")
	}

	val scoreboards = ConcurrentHashMap<Player, PlayerScoreboard>()
    val coloredGlow = ConcurrentHashMap<UUID, ChatColor>()
	var cachedClubesPrefixes = WeakHashMap<Player, String?>()

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)

		registerCommand(EventosCommand)
		registerCommand(GlowingCommand)
		registerCommand(GlowingColorCommand)

		scheduler().schedule(this, SynchronizationContext.SYNC) {
			while (true) {
				// Carregar tag de clubes
				switchContext(SynchronizationContext.ASYNC)
				val prefixes = WeakHashMap<Player, String?>()
				Bukkit.getOnlinePlayers().forEach {
					prefixes[it] = ClubeAPI.getPlayerClube(it)?.shortName
				}
				switchContext(SynchronizationContext.SYNC)
				cachedClubesPrefixes = prefixes

				Bukkit.getOnlinePlayers().forEach {
					if (scoreboards[it] == null) {
						val playerScoreboard = PlayerScoreboard(this@DreamScoreboard, it)
						playerScoreboard.updateScoreboard()
						scoreboards[it] = playerScoreboard
					}
				}
				waitFor(20 * 15)
			}
		}

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				CURRENT_TICK++
				if (CURRENT_TICK > 19) {
					CURRENT_TICK = 0
				}
				scoreboards.values.forEach {
					try {
						it.updateScoreboard()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
				waitFor(20)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		val playerScoreboard = PlayerScoreboard(this, e.player)
		playerScoreboard.updateScoreboard()
		scoreboards[e.player] = playerScoreboard
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		scoreboards.remove(e.player)
	}
}