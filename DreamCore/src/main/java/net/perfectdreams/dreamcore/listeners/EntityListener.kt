package net.perfectdreams.dreamcore.listeners

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import net.perfectdreams.dreamcore.utils.ArmorStandHologram
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityListener : Listener {
	@EventHandler
	fun onEntityAdd(e: EntityAddToWorldEvent) {
		val entity = e.entity

		if (entity !is ArmorStand)
			return

		val markedForRemoval = ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS[entity.uniqueId] ?: return

		if (markedForRemoval) {
			entity.remove()
			ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS.remove(entity.uniqueId)
			ArmorStandHologram.updateFile()
		}
	}

	@EventHandler
	fun onEntityKill(e: EntityDeathEvent) {
		val entity = e.entity

		if (entity !is ArmorStand)
			return

		if (ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS.contains(entity.uniqueId)) {
			ArmorStandHologram.ARMOR_STANDS_UNIQUE_IDS.remove(entity.uniqueId)
			ArmorStandHologram.updateFile()
		}
	}
}