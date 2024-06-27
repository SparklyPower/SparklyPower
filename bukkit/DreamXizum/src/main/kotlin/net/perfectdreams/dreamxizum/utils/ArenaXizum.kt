package net.perfectdreams.dreamxizum.utils

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.broadcast
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffectsAwait
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamxizum.DreamXizum
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class ArenaXizum(val m: DreamXizum, val data: ArenaXizumData) {
	val location1 = Location(
		Bukkit.getWorld(data.location1.world),
		data.location1.x,
		data.location1.y,
		data.location1.z,
		data.location1.yaw,
		data.location1.pitch
	)

	val location2 = Location(
		Bukkit.getWorld(data.location2.world),
		data.location2.x,
		data.location2.y,
		data.location2.z,
		data.location2.yaw,
		data.location2.pitch
	)

	var player1: Player? = null
	var player2: Player? = null
	var lastEventId = UUID(0, 0)
	var isCountingDown = false

	fun startArena(player1: Player, player2: Player) {
		broadcast(DreamXizum.PREFIX + " §b${player1.displayName}§e §4§lVS §b${player2.displayName}")

		val randEventId = UUID.randomUUID()
		lastEventId = randEventId

		this.player1 = player1
		this.player2 = player2

		player1.health = 20.0
		player1.foodLevel = 20
		player2.health = 20.0
		player2.foodLevel = 20
		player1.gameMode = GameMode.SURVIVAL
		player2.gameMode = GameMode.SURVIVAL

		// Teletransportar os players para os starting points
		if (!player1.teleport(location1)) {
			// uh oh, finish arena because the player wasn't teleported
			finishArena(player1, WinType.DISCONNECTED)
			return
		}

		if (!player2.teleport(location2)) {
			// uh oh, finish arena because the player wasn't teleported
			finishArena(player2, WinType.DISCONNECTED)
			return
		}

		player1.walkSpeed = 0f
		player2.walkSpeed = 0f
		player1.removeAllPotionEffects()
		player2.removeAllPotionEffects()
		player1.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 100, -5))
		player2.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 100, -5))

		scheduler().schedule(m) {
			isCountingDown = true
			for (idx in 5 downTo 1) {
				player1.sendTitle("§a§l$idx", "", 0, 15, 5)
				player2.sendTitle("§a§l$idx", "", 0, 15, 5)
				waitFor(20)
			}
			isCountingDown = false
			player1.walkSpeed = 0.2f
			player2.walkSpeed = 0.2f
			player1.sendTitle("§c§lLutem!", "", 0, 15, 5)
			player2.sendTitle("§c§lLutem!", "", 0, 15, 5)

			m.launchMainThread {
				while (lastEventId == randEventId) {
					val player1 = this@ArenaXizum.player1
					val player2 = this@ArenaXizum.player2

					if (player1 == null || player2 == null)
						break

					if (!player1.isValid || !player1.isOnline) {
						finishArena(player1, WinType.DISCONNECTED)
						break
					}

					if (!player2.isValid || !player2.isOnline) {
						finishArena(player2, WinType.DISCONNECTED)
						break
					}

					if (player1.world.name != location1.world.name) {
						finishArena(player1, WinType.KILLED)
						break
					}

					if (player2.world.name != location2.world.name) {
						finishArena(player2, WinType.KILLED)
						break
					}

					delayTicks(20L)
				}
			}

			scheduler().schedule(m) {
				var seconds = 180
				while (lastEventId == randEventId) {
					val player1 = this@ArenaXizum.player1
					val player2 = this@ArenaXizum.player2

					if (player1 == null || player2 == null)
						break

					if (0 >= seconds) {
						finishArena(player2, WinType.TIMEOUT)
						break
					}

					player1.sendMessage(DreamXizum.PREFIX + " §3Faltam §d${seconds} segundos§3 para acabar a partida!")
					player2.sendMessage(DreamXizum.PREFIX + " §3Faltam §d${seconds} segundos§3 para acabar a partida!")

					waitFor(20 * 15)
					seconds -= 15
				}
			}
		}
	}

	fun finishArena(loser: Player, type: WinType) {
		lastEventId = UUID(0, 0)

		// Okay, ele estava... fazer o que né
		val winner = if (loser == player1) {
			player2
		} else {
			player1
		}!!

		winner.health = 20.0
		winner.foodLevel = 20
		winner.fireTicks = 0
		loser.health = 20.0
		loser.foodLevel = 20
		loser.fireTicks = 0

		if (type == WinType.DISCONNECTED) {
			broadcast(DreamXizum.PREFIX + " §b${loser.displayName}§e arregou o Xizum contra o §b${winner.displayName}§e!")
		} else if (type == WinType.KILLED) {
			broadcast(DreamXizum.PREFIX + " §b${winner.displayName}§e venceu o Xizum contra o §b${loser.displayName}§e!")
		} else if (type == WinType.TIMEOUT) {
			broadcast(DreamXizum.PREFIX + " §b${winner.displayName}§e e §b${loser.displayName}§e demoraram tanto que a partida do Xizum acabou...")
		}

		winner.sendTitle("§a§lVocê venceu!", "§f", 10, 80, 10)

		val loserLocation = loser.location

		val head = ItemStack(Material.PLAYER_HEAD, 1)
		val meta = head.itemMeta as SkullMeta
		meta.playerProfile = loser.playerProfile
		head.itemMeta = meta
		val item = loserLocation.world.dropItemNaturally(loserLocation, head)

		m.launchMainThread {
			// We need to be in a separate thread because...
			// 1. this is async
			// 2. PlayerDeathEvent runs on a server level ticking thread and that causes issues with parallel world ticking (and Folia)
			loser.teleportToServerSpawnWithEffectsAwait()
		}

		winner.playSound(winner.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

		InstantFirework.spawn(loserLocation, FireworkEffect.builder()
				.with(FireworkEffect.Type.STAR)
				.withColor(Color.RED)
				.withFade(Color.BLACK)
				.withFlicker()
				.withTrail()
				.build())

		InstantFirework.spawn(winner.location, FireworkEffect.builder()
				.with(FireworkEffect.Type.STAR)
				.withColor(Color.GREEN)
				.withFade(Color.BLACK)
				.withFlicker()
				.withTrail()
				.build())

		m.launchMainThread {
			delayTicks(100)
			item.remove()
			winner.teleportToServerSpawnWithEffectsAwait()
			player1 = null
			player2 = null
		}
	}
}