package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

class DisablePhantomsListener(val m: DreamMini) : Listener {
	@EventHandler
	fun onSpawn(e: EntitySpawnEvent) {
		if (e.entityType == EntityType.PHANTOM) {
			e.isCancelled = true
		}
	}
}