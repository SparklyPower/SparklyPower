package net.perfectdreams.dreamcore.utils

import com.comphenix.packetwrapper.WrapperPlayServerAttachEntity
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import org.bukkit.Location
import org.bukkit.entity.Player
import net.minecraft.world.entity.Entity
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerSetOf
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.EntityType
import java.util.*

/**
 * Class to spawn holograms using packets
 * @author MrPowerGamerBR
 *
 */
class Hologram(val location: Location, var text: String) {
	// TODO: #addLine(), #removeLine(), #teleport()
	companion object {
		private val byteSerializer = WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)
		private val chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true)
		private val booleanSerializer = WrappedDataWatcher.Registry.get(java.lang.Boolean::class.java)

		private val invisible = WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer)
		private val name = WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer)
		private val showName = WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer)
		private val gravity = WrappedDataWatcher.WrappedDataWatcherObject(5, booleanSerializer)
	}

	private val ID = Entity.nextEntityId()

	private val armorStand = WrapperPlayServerSpawnEntity().apply {
		entityID = ID
		uniqueId = UUID.randomUUID()
		x = location.x
		y = location.y
		z = location.z
		type = EntityType.ARMOR_STAND
	}

	private val dataWatcher = WrappedDataWatcher().apply {
		setObject(invisible, 0x20.toByte()) // Invisible
		setObject(name, asWrappedName(text)) // Name
		setObject(showName, java.lang.Boolean.TRUE) // Always show nametag
		setObject(gravity, java.lang.Boolean.TRUE) // No gravity
	}

	private val metadata = WrapperPlayServerEntityMetadata().apply {
		entityID = ID
		metadata = dataWatcher.watchableObjects
	}

	private val attach = WrapperPlayServerAttachEntity().apply { entityID = ID }
	private val remove = ClientboundRemoveEntitiesPacket(ID)

	fun updateText(text: String, player: Player) {
		this.text = text
		dataWatcher.setObject(name, asWrappedName(text))
		metadata.metadata = dataWatcher.watchableObjects
		metadata.sendPacket(player)
	}

	fun spawnTo(player: Player) {
		armorStand.sendPacket(player)
		metadata.sendPacket(player)
	}

	fun attachTo(target: Int, player: Player) {
		attach.vehicleId = target
		attach.sendPacket(player)
	}

	fun detachTo(player: Player) {
		attach.vehicleId = -1
		attach.sendPacket(player)
	}

	fun despawnTo(player: Player) = (player as CraftPlayer).handle.connection.send(remove)
	private fun asWrappedName(name: String) = Optional.of(WrappedChatComponent.fromChatMessage(name)[0].handle)
}

class WrapperHologram(val location: Location, lines: MutableList<String>) {
	private val viewers = mutablePlayerSetOf()
	private val holograms = LinkedList<Hologram>()
	var lines: MutableList<String> = lines
		set(value) {
			field = value
			field.forEachIndexed { index, line ->
				viewers.forEach { holograms[index].updateText(line, it) }
			}
		}

	init {
		val originalY = location.y
		lines.forEachIndexed { index, it ->
			if (index > 0) location.y -= 0.285
			holograms.add(Hologram(location, it))
		}
		location.y = originalY
	}

	fun setLine(index: Int, line: String) = viewers.forEach {
		lines[index] = line
		holograms[index].updateText(line, it)
	}

	fun addViewer(player: Player) {
		viewers.add(player)
		holograms.forEach { it.spawnTo(player) }
	}

	fun removeViewer(player: Player) {
		viewers.remove(player)
		holograms.forEach { it.despawnTo(player) }
	}
}