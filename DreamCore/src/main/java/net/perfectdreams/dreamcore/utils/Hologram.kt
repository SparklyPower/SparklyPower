package net.perfectdreams.dreamcore.utils

import com.comphenix.packetwrapper.*
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedWatchableObject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Class to spawn holograms in 1.12.2 using packets
 * @author MrPowerGamerBR
 */
class Hologram(var location: Location, internal var line: String?) {
	var horseId = 0 // Also used as the Armor Stand ID
	var viewers = ConcurrentHashMap.newKeySet<Player>()
	var vh: ViewHandler? = null

	private val id: Int
		get() {
			try {
				val field = Class.forName(
						"net.minecraft.server." + Bukkit.getServer().javaClass.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3] + ".Entity")
						.getDeclaredField("entityCount")
				field.isAccessible = true
				val id = field.getInt(null)
				field.set(null, id + 1)
				return id
			} catch (ex: Exception) {
				ex.printStackTrace()
			}

			return -1
		}

	init {
		horseId = id
	}

	fun setLine(line: String) {
		this.line = line

		for (p in viewers) {
			if (p.world == location.world) {
				var packets: List<AbstractPacket>? = null

				packets = generateMetadataPackets(p)

				for (packet in packets) {
					packet.sendPacket(p)
				}
			}
		}
	}

	fun getLineForPlayer(p: Player?): String? {
		return if (vh != null) {
			vh!!.onView(this, p, line)
		} else line
	}

	private fun generateMetadataPackets(p: Player?): List<AbstractPacket> {
		val metadata = WrapperPlayServerEntityMetadata()
		metadata.entityID = horseId

		val stringSerializer = WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String::class.javaObjectType))

		metadata.metadata = listOf(
				WrappedWatchableObject(stringSerializer, getLineForPlayer(p))
		)

		return Arrays.asList<AbstractPacket>(metadata)
	}

	fun addEveryoneAsViewer() {
		for (p in Bukkit.getOnlinePlayers()) {
			addViewer(p)
		}
	}

	fun removeEveryoneFromViewers() {
		for (p in viewers) {
			despawnForPlayer(p)
			viewers.remove(p)
		}
	}

	fun addViewer(p: Player) {
		viewers.add(p)

		spawnForPlayer(p)
	}

	fun removeViewer(p: Player) {
		viewers.remove(p)

		despawnForPlayer(p)
	}

	fun spawnForAll() {
		if (line == null) {
			throw RuntimeException("Line is null!")
		}

		for (p in viewers) {
			if (p.world == location.world) {
				spawnForPlayer(p)
			}
		}
	}

	fun spawnForPlayer(p: Player?) {
		if (p!!.location.world != location.world) {
			return
		}
		var packets: List<AbstractPacket>? = null

		packets = generateSpawnPackets(p)

		for (packet in packets) {
			if (p.world == location.world) {
				packet.sendPacket(p)
			}
		}
	}

	private fun generateSpawnPackets(p: Player): List<AbstractPacket> {
		// Because Armor Stand is a living entity...
		val armorStand = WrapperPlayServerSpawnEntityLiving()

		armorStand.entityID = horseId
		armorStand.x = location.x
		armorStand.y = location.y + 1.52
		armorStand.z = location.z
		armorStand.type = EntityType.ARMOR_STAND // Armor Stand ID

		val byteSerializer = WrappedDataWatcher.Registry.get(Byte::class.javaObjectType)
		val stringSerializer = WrappedDataWatcher.Registry.get(String::class.javaObjectType)
		val booleanSerializer = WrappedDataWatcher.Registry.get(Boolean::class.javaObjectType)

		val wdw = WrappedDataWatcher()

		wdw.setObject(0, byteSerializer, 0x20.toByte()) // Invisible
		wdw.setObject(2, stringSerializer, getLineForPlayer(p)) // Name
		wdw.setObject(3, booleanSerializer, true as Any) // Always show nametag
		wdw.setObject(5, booleanSerializer, true as Any) // No gravity
		val bitmask = 0x10 // Zero Bounding Box (Marker)
		wdw.setObject(11, byteSerializer, bitmask.toByte()) // Set our metadatas

		armorStand.metadata = wdw

		val packets = Arrays.asList<AbstractPacket>(armorStand)
		return packets
	}

	fun despawnForPlayer(p: Player?) {
		val remove = WrapperPlayServerEntityDestroy()

		remove.setEntityIds(intArrayOf(horseId))

		if (p!!.world == location.world) {
			remove.sendPacket(p)
		}
	}

	fun despawnForAll() {
		val remove = WrapperPlayServerEntityDestroy()

		remove.setEntityIds(intArrayOf(horseId))

		for (p in viewers) {
			if (p.world == location.world) {
				remove.sendPacket(p)
			}
		}
	}

	fun teleport(l: Location) {
		this.location = l

		// Optimizing teleport: Create only one packet and then send to everyone
		val teleport = WrapperPlayServerEntityTeleport()
		teleport.entityID = horseId
		teleport.x = l.x
		teleport.y = l.y + 1.52
		teleport.z = l.z

		for (p in viewers) {
			if (p.world == location.world) { // Only send to the same world
				teleport.sendPacket(p)
			}
		}
	}

	fun teleport(p: Player, l: Location) {
		this.location = l

		// Not so optimized: Do not use for (Player p : Bukkit.getOnlinePlayers()) { teleport(p, l); } for this! Use teleport(l);
		val teleport = WrapperPlayServerEntityTeleport()
		teleport.entityID = horseId
		teleport.x = l.x
		teleport.y = l.y + 1.52
		teleport.z = l.z

		if (p.world == location.world) {
			teleport.sendPacket(p)
		}
	}

	fun teleport(players: List<Player>, l: Location) {
		this.location = l

		// Optimizing teleport: Create only one packet and then send to everyone
		val teleport = WrapperPlayServerEntityTeleport()
		teleport.entityID = horseId
		teleport.x = l.x
		teleport.y = l.y + 1.52
		teleport.z = l.z

		for (p in players) {
			if (p.world == location.world) {
				teleport.sendPacket(p)
			}
		}
	}

	fun addLineBelow(line: String): Hologram {
		val hologram = Hologram(location.clone().add(0.0, -0.285, 0.0), line)
		return hologram
	}

	fun addLineAbove(line: String): Hologram {
		val hologram = Hologram(location.clone().add(0.0, 0.285, 0.0), line)
		return hologram
	}

	fun attachTo(entity: Entity) {
		val wpsae = WrapperPlayServerAttachEntity()
		wpsae.vehicleId = entity.entityId
		wpsae.entityID = horseId

		for (p in viewers) {
			if (p.world == location.world) {
				wpsae.sendPacket(p)
			}
		}
	}

	fun unattach() {
		val wpsae = WrapperPlayServerAttachEntity()
		wpsae.vehicleId = -1
		wpsae.entityID = horseId

		for (p in viewers) {
			if (p.world == location.world) {
				wpsae.sendPacket(p)
			}
		}
	}
}

class ViewHandler {
	fun onView(hologram: Hologram, player: Player?, string: String?): String? {
		return string
	}
}