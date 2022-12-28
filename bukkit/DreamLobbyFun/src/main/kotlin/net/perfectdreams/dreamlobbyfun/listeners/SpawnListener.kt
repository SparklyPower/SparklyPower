package net.perfectdreams.dreamlobbyfun.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamauth.events.PlayerLoggedInEvent
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.DreamCore.Companion.dreamConfig
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import net.perfectdreams.dreamlobbyfun.dao.PlayerSettings
import net.perfectdreams.dreamlobbyfun.tables.UserSettings
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.exposed.sql.transactions.transaction

class SpawnListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		e.joinMessage = null
		e.player.inventory.clear()
	}

	@EventHandler
	fun onLogin(e: PlayerLoggedInEvent) {
		e.player.teleportToServerSpawnWithEffects()

		handleJoin(e.player)
	}

	@EventHandler
	fun onRespawn(e: PlayerRespawnEvent) {
		val dreamAuth = m.getDreamAuthInstance()
		val playerStatus = dreamAuth.playerStatus[e.player]

		// Only teleport to the server spawn if the player is logged in!
		if (playerStatus == PlayerStatus.LOGGED_IN) {
			e.respawnLocation = DreamCore.INSTANCE.spawn!!
		} else {
			e.respawnLocation = dreamAuth.authConfig.loginLocation ?: error("DreamAuth Login Location is not present!")
		}

		handleJoin(e.player)
	}

	@EventHandler
	fun onLeave(e: PlayerQuitEvent) {
		e.quitMessage = null
		m.unlockedPlayers.remove(e.player)
	}

	fun handleJoin(player: Player) {
		player.foodLevel = 20
		player.health = 20.0

		// Spawnar fireworks com cores aleatórias quando o player entrar no servidor
		val r = DreamUtils.random.nextInt(0, 256)
		val g = DreamUtils.random.nextInt(0, 256)
		val b = DreamUtils.random.nextInt(0, 256)

		val fadeR = Math.max(0, r - 60)
		val fadeG = Math.max(0, g - 60)
		val fadeB = Math.max(0, b - 60)

		val fireworkEffect = FireworkEffect.builder()
				.withTrail()
				.withColor(Color.fromRGB(r, g, b))
				.withFade(Color.fromRGB(fadeR, fadeG, fadeB))
				.with(FireworkEffect.Type.values()[DreamUtils.random.nextInt(0, FireworkEffect.Type.values().size)])
				.build()

		val firework = player.world.spawnEntity(player.location, EntityType.FIREWORK) as Firework
		val fireworkMeta = firework.fireworkMeta

		fireworkMeta.power = 1
		fireworkMeta.addEffect(fireworkEffect)

		firework.fireworkMeta = fireworkMeta

		player.compassTarget = Location(player.world, 0.5, 184.0, 155.0)

		player.addPotionEffect(
				PotionEffect(PotionEffectType.SPEED, 1000000, 1, true, false)
		)

		m.songPlayer?.addPlayer(player)

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val playerInfo = transaction(Databases.databaseNetwork) {
				val thingy = PlayerSettings.findById(player.uniqueId)
				thingy ?: PlayerSettings.new(player.uniqueId) {
					playerVisibility = true
				}
			}

			val playerVisibility = playerInfo.playerVisibility

			val currentJoinedPlayersInfo = transaction(Databases.databaseNetwork) {
				PlayerSettings.find { UserSettings.id inList Bukkit.getOnlinePlayers().map { it.uniqueId } }.toMutableList()
			}

			switchContext(SynchronizationContext.SYNC)

			if (!playerVisibility) {
				for (i in Bukkit.getOnlinePlayers()) {
					player.hidePlayer(m, i)
				}
			}

			for (i in Bukkit.getOnlinePlayers()) {
				val loopPlayerInfo = currentJoinedPlayersInfo.firstOrNull { it.id.value == i.uniqueId } ?: continue

				val loopPlayerVisibility = loopPlayerInfo.playerVisibility

				if (!loopPlayerVisibility) {
					i.hidePlayer(m, player)
				}
			}

			m.giveLobbyItems(player, playerInfo)
		}
	}
}