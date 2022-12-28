package net.perfectdreams.dreamlobbyfun.listeners

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.extensions.getSafeDestination
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerQuitEvent

class TeleportBowListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		// Automatically delete all arrows that were shot by the player
		e.player.world.entities
			.filterIsInstance<Arrow>()
			.filter { it.shooter == e.player }
			.onEach { it.remove() }
	}

	@EventHandler
	fun onShoot(e: EntityShootBowEvent) {
		val projectile = e.projectile

		if (projectile !is Arrow)
			return

		val shooter = projectile.shooter

		if (shooter !is Player) // We only care if it is a player shooting the bow
			return

		projectile.isGlowing = true
		projectile.setBounce(false)

		scheduler().schedule(m) {
			// We need to wait 1 tick, since the projectile is not "valid" here
			waitFor(1)

			while (projectile.isValid && shooter.isOnline) { // Spawn the particles while the projectile is valid AND the player that shot it is still online
				projectile.world.spawnParticle(Particle.LAVA, projectile.location, 1, 0.0, 0.0, 0.0, 0.1)
				projectile.world.spawnParticle(Particle.FLAME, projectile.location, 1, 0.0, 0.0, 0.0, 0.1)
				waitFor(5)
			}

			projectile.remove() // Clean up, this may also be triggered if the player shot it is offline, so that's why we remove the arrows here too
		}
	}

	@EventHandler
	fun onCollide(e: ProjectileCollideEvent) { // Vamos fazer que as flechas passem por entidades
		e.isCancelled = true
		e.entity.velocity = e.entity.velocity // isto irá fazer o servidor reenviar a velocidade da flecha, para evitar bugs visuais
	}

	@EventHandler
	fun onHit(e: ProjectileHitEvent) {
		if (e.entity is Arrow) {
			val location = e.hitBlock?.location

			if (e.entity.shooter != null && location != null) {
				val shooter = e.entity.shooter

				if (shooter is Player) {
					e.entity.passengers.filter { it !is Player }.forEach {
						it.remove()
					}

					e.entity.remove()

					// Ao atingir, vamos procurar um lugar seguro e vazio para o player teletransportar
					val safeDestination = try {
						location.add(0.0, 1.0, 0.5).getSafeDestination()
					} catch (e: LocationUtils.HoleInFloorException) {
						return
					}

					if (location.isWithinRegion(FunPvPListener.REGION_NAME)) {
						shooter.sendMessage("§cInfelizmente a flecha caiu beeeeem na super arena PvP... a gente não pode te colocar em uma área tão insegura como essa!")
						return
					}

					shooter.teleport(safeDestination)
					shooter.playSound(safeDestination, Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f)

					shooter.sendMessage("§6(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ §eWoosh! §6✧ﾟ･: *ヽ(◕ヮ◕ヽ)")
				}
			}
		}
	}
}