package net.perfectdreams.dreamtrails.listeners

import net.perfectdreams.dreamtrails.DreamTrails
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamtrails.utils.Trails
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveListener(val m: DreamTrails) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onMove(e: PlayerMoveEvent) {
		if (!e.displaced)
			return

		if (e.player.location.world.name == "Quiz")
			return

		val activeTrails = m.playerTrails[e.player.uniqueId] ?: return

		activeTrails.activeParticles.forEach { activeParticle ->
			val trailData = Trails.trails[activeParticle]

			if (trailData != null) {
				if (trailData.cooldown != 0L) {
					val theLastTimeTheEffectWasPlayed = activeTrails.cooldowns.getOrPut(activeParticle, { 0L })

					val diff = System.currentTimeMillis() - theLastTimeTheEffectWasPlayed

					if (diff > trailData.cooldown) {
						activeTrails.cooldowns[trailData.particle] = System.currentTimeMillis()
					} else {
						return@forEach
					}
				}

				var location = e.player.location

				if (trailData.locationDirectionOffset != 0.0) {
					location = e.player.location.add(
						e.player.location.direction
							.setY(0)
							.multiply(trailData.locationDirectionOffset)
					)
				}

				location = location.add(
					trailData.locationOffsetX,
					trailData.locationOffsetY,
					trailData.locationOffsetZ
				)

				e.player.world.spawnParticle(
					trailData.particle,
					location,
					trailData.count,
					trailData.offsetX,
					trailData.offsetY,
					trailData.offsetZ
				)
			}
		}
	}
}