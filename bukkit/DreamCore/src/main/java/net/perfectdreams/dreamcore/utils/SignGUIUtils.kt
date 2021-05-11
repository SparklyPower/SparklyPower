package net.perfectdreams.dreamcore.utils

import com.comphenix.packetwrapper.WrapperPlayClientUpdateSign
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange
import com.comphenix.packetwrapper.WrapperPlayServerOpenSignEditor
import com.comphenix.packetwrapper.WrapperPlayServerTileEntityData
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

object SignGUIUtils {
	// Maps the UUID of a Player to the Location of the SignGUI that he opened
	private val signListeners = WeakHashMap<Player, SignGUIListener>()

	/**
	 * Register a Listener to react to the [Player] closing the GUI.
	 */
	fun registerSignGUIListener() {
		val manager = ProtocolLibrary.getProtocolManager()
		manager.addPacketListener(object : PacketAdapter(Bukkit.getPluginManager().getPlugin("DreamCore"), PacketType.Play.Client.UPDATE_SIGN) {
			override fun onPacketReceiving(event: PacketEvent) {
				val wrapper = WrapperPlayClientUpdateSign(event.packet)
				val blockPos = wrapper.location
				val listener = signListeners[event.player]

				if (listener != null && blockPos.x.toDouble() == listener.location!!.x && blockPos.y.toDouble() == listener.location!!.y && blockPos.z.toDouble() == listener.location!!.z) {
					// Do anything here
					fixFakeBlockFor(event.player, listener.location)
					listener.onSignDone(event.player, wrapper.lines)
					signListeners.remove(event.player)
				}
			}
		})
	}

	/**
	 * Fixes the Block at the [Location] of a SignGUI a [Player] opened.
	 * @param player The [Player].
	 * @param loc The [Location] of the Block that has to be fixed for the [Player].
	 */
	internal fun fixFakeBlockFor(player: Player, loc: Location?) {
		// Check if the Player is in the same World of the fake Block.
		// If the Player is in another world, the fake Block will be fixed automatically when the Player loads its world.
		if (loc!!.world != null && player.world == loc.world) {
			val manager = ProtocolLibrary.getProtocolManager()

			val wrapperBlockChange = WrapperPlayServerBlockChange(manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE))
			// WrapperPlayServerTileEntityData wrapperTileEntityData = new WrapperPlayServerTileEntityData(manager.createPacket(PacketType.Play.Server.TILE_ENTITY_DATA));

			val material = loc.world.getBlockAt(loc.blockX, loc.blockY, loc.blockZ).type
			val blockData = WrappedBlockData.createData(material, loc.world.getBlockAt(loc.blockX, loc.blockY, loc.blockZ).data.toInt())
			wrapperBlockChange.location = BlockPosition(loc.blockX, loc.blockY, loc.blockZ)
			wrapperBlockChange.blockData = blockData

			wrapperBlockChange.sendPacket(player)
		}
	}

	/**
	 * Opens a Sign GUI for a[Player].
	 * @param player The [Player]
	 * @param line1 The first line which should be displayed on the GUI.
	 * @param line2 The second line which should be displayed on the GUI.
	 * @param line3 The third line which should be displayed on the GUI.
	 * @param line4 The fourth line which should be displayed on the GUI.
	 */
	fun openGUIFor(player: Player, lines: Array<String>, listener: SignGUIListener) {
		val manager = ProtocolLibrary.getProtocolManager()

		// The Position has to be near the Player. Otherwise the BlockChange Packet will be ignore by the Client.
		val pos = BlockPosition(player.location.blockX, 0, player.location.blockZ)

		// The Nbt Data used to display custom Text in the GUI
		val signNbt = NbtFactory.ofCompound("AnyStringHere")
		signNbt.put("Text1", "{\"text\":\"${lines[0]}\"}")
		signNbt.put("Text2", "{\"text\":\"${lines[1]}\"}")
		signNbt.put("Text3", "{\"text\":\"${lines[2]}\"}")
		signNbt.put("Text4", "{\"text\":\"${lines[3]}\"}")
		signNbt.put("id", "minecraft:sign")
		signNbt.put("x", pos.x)
		signNbt.put("y", pos.y)
		signNbt.put("z", pos.z)

		// Wrapper that sends the Player a fake Sign Block
		val wrapperBlockChange = WrapperPlayServerBlockChange(manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE))
		// Wrapper that lets the Player open a Sign GUI
		val wrapperOpenSignEditor = WrapperPlayServerOpenSignEditor(manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR))
		// Wrapper that sets the NBT Data of the previously sent fake Sign Block
		val wrapperTileEntityData = WrapperPlayServerTileEntityData(manager.createPacket(PacketType.Play.Server.TILE_ENTITY_DATA))

		// Add the Position and BlockData to the BlockChange Packet
		wrapperBlockChange.location = pos
		wrapperBlockChange.blockData = WrappedBlockData.createData(Material.OAK_SIGN)

		// Add the Position to the OpenSignEditor Packet
		wrapperOpenSignEditor.location = pos

		// Add Nbt, Action and Position to the TileEntityDataPacket
		wrapperTileEntityData.nbtData = signNbt
		wrapperTileEntityData.action = 9
		wrapperTileEntityData.location = pos

		// Send the Packets
		wrapperBlockChange.sendPacket(player)
		wrapperOpenSignEditor.sendPacket(player)
		wrapperTileEntityData.sendPacket(player)

		// Save the Position where the Sign was placed
		listener.location = Location(player.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
		signListeners.put(player, listener)
	}

	abstract class SignGUIListener {
		var location: Location? = null

		abstract fun onSignDone(player: Player, lines: Array<String>)
	}
}