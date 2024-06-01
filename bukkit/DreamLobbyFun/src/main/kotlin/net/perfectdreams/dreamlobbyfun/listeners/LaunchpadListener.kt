package net.perfectdreams.dreamlobbyfun.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector

class LaunchpadListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onMove(event: PlayerMoveEvent) {
		if (event.from.x == event.to.x && event.from.y == event.to.y && event.from.z == event.to.z) // Vamos ignorar se o player não moveu
			return

		val launchPadBlock = event.to.block

		if (launchPadBlock.type == Material.STONE_PRESSURE_PLATE && launchPadBlock.getRelative(BlockFace.DOWN).type == Material.REDSTONE_BLOCK) {
			val diff = System.currentTimeMillis() - m.launchPadDelay.getOrDefault(event.player, 0L)

			if (1000 > diff) {
				return
			}

			m.launchPadDelay[event.player] = System.currentTimeMillis()

			var multiplyValue = 2.25
			var yValue = 0.75

			// Vamos fazer que seja possível customizar os valores do launchpad usando... placas!
			val signBlock = launchPadBlock.getRelative(BlockFace.DOWN, 2)
			if (signBlock?.type?.name?.contains("SIGN") == true) {
				val signState = signBlock.state as Sign

				if (signState.getLine(0) == "[Launchpad]") {
					multiplyValue = signState.getLine(1).toDouble()
					yValue = signState.getLine(2).toDouble()
				}
			}

			val player = event.player
			player.world.spawnParticle(Particle.CLOUD, player.location, 30, 0.0, 0.0, 0.0, 0.1)

			val yaw = player.location.yaw
			val newVelocity = player.velocity.clone()
			val speed = Vector(0f, 0f, 0f)

			val radiiYaw = Math.toRadians(yaw.toDouble())

			val sinYaw = Math.sin(-radiiYaw)
			val cosYaw = Math.cos(radiiYaw)

			speed.x = sinYaw * multiplyValue
			speed.z = cosYaw * multiplyValue

			newVelocity.y = yValue

			newVelocity.add(speed)

			player.velocity = newVelocity

			player.world.playSound(player.location, Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.0f)

			player.sendMessage("§6(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ §eWoosh! §6✧ﾟ･: *ヽ(◕ヮ◕ヽ)")

			InstantFirework.spawn(
					player.location.add(0.0, 1.0, 0.0),
					FireworkEffect.builder().with(
							FireworkEffect.Type.BALL_LARGE
					).withColor(org.bukkit.Color.AQUA)
							.withFade(org.bukkit.Color.BLUE)
							.build()
			)

			scheduler().schedule(m) {
				waitFor(5)
				while (player.isValid && !player.isOnGround) {
					val dustOptions = Particle.DustOptions(Color.TEAL, 3f)
					player.world.spawnParticle(Particle.FIREWORK, player.location, 1, 0.0, 0.0, 0.0, 0.1)
					player.world.spawnParticle(Particle.DUST, player.location, 1, 0.0, 0.0, 0.0, 0.1, dustOptions)
					waitFor(1)
				}
			}
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		m.launchPadDelay.remove(e.player)
	}
}