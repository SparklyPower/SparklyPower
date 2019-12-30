package net.perfectdreams.dreamcore.utils

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import java.io.File
import java.util.*

/**
 * Classe para spawnar hologramas usando armor stands
 *
 * @author MrPowerGamerBR
 */
class ArmorStandHologram(var location: Location, internal var line: String?) {
	companion object {
		// armor stand unique ID -> marked for removal
		val ARMOR_STANDS_UNIQUE_IDS = mutableMapOf<UUID, Boolean>()

		private val ARMOR_STAND_FILE by lazy {
			val plugin = Bukkit.getPluginManager().getPlugin("DreamCore")!!
			plugin.dataFolder.mkdirs()
			File(plugin.dataFolder, "armor_stand_holograms")
		}

		/**
		 * Adiciona uma armor stand a lista de IDs de hologramas criados com armor stands
		 *
		 * Utilizado para deletar todas as armor stands quando o servidor reiniciar
		 */
		@Synchronized
		fun addUniqueId(uniqueId: UUID) {
			ARMOR_STANDS_UNIQUE_IDS[uniqueId] = false
			updateFile()
		}

		@Synchronized
		fun updateFile() {
			scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamCore")!!, SynchronizationContext.ASYNC) {
				ARMOR_STAND_FILE.writeText(
						ARMOR_STANDS_UNIQUE_IDS.keys.joinToString("\n")
				)
			}
		}

		/**
		 * Carrega todos os IDs das armor stands
		 */
		fun loadArmorStandsIdsMarkedForRemoval() {
			if (!ARMOR_STAND_FILE.exists())
				return

			val uniqueIds = ARMOR_STAND_FILE.readLines().map { UUID.fromString(it) }

			uniqueIds.forEach {
				ARMOR_STANDS_UNIQUE_IDS[it] = true
			}
		}
	}

	var armorStand: ArmorStand? = null

	fun spawn() {
		if (isSpawned()) {
			armorStand?.remove()
		}

		val stand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
		stand.customName = line
		stand.isCustomNameVisible = true
		stand.isMarker = true
		stand.isVisible = false
		stand.setGravity(false)

		armorStand = stand

		addUniqueId(stand.uniqueId)
	}

	fun despawn() {
		armorStand?.remove()
		armorStand = null
	}

	fun teleport(newLocation: Location) {
		armorStand?.teleport(newLocation)
		location = newLocation
	}

	fun setLine(newLine: String) {
		armorStand?.customName = line
		line = newLine
	}

	fun addLineBelow(line: String, yOffset: Double = -0.285): ArmorStandHologram {
		val hologram = ArmorStandHologram(location.clone().add(0.0, yOffset, 0.0), line)
		return hologram
	}

	fun addLineAbove(line: String, yOffset: Double = 0.285): ArmorStandHologram {
		val hologram = ArmorStandHologram(location.clone().add(0.0, yOffset, 0.0), line)
		return hologram
	}

	fun isSpawned(): Boolean {
		val stand = armorStand ?: return false

		if (!ARMOR_STANDS_UNIQUE_IDS.containsKey(stand.uniqueId)) { // Se a armor stand existe, mas ela não existe na nossa lista de UUIDs
			stand.remove()
			return false
		}

		if (ARMOR_STANDS_UNIQUE_IDS[stand.uniqueId] == true) { // Se a armor stand existe, mas ela está marcada para ser removida
			stand.remove()
			ARMOR_STANDS_UNIQUE_IDS.remove(stand.uniqueId)
			updateFile()
			return false
		}

		if (!stand.isValid) { // Se a armor stand existe, mas ela não está mais válida
			stand.remove()
			ARMOR_STANDS_UNIQUE_IDS.remove(stand.uniqueId)
			updateFile()
			return false
		}

		return true
	}
}