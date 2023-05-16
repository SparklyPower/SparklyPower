package net.perfectdreams.dreambedrockintegrations

import com.comphenix.protocol.ProtocolLibrary
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import fr.neatmonster.nocheatplus.checks.CheckType
import fr.neatmonster.nocheatplus.hooks.NCPHookManager
import net.kyori.adventure.text.Component
import net.perfectdreams.dreambedrockintegrations.packetlisteners.BedrockPacketListener
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import java.util.*

class DreamBedrockIntegrations : KotlinPlugin(), Listener {
	private val ncpHook = Hook(this)
	val geyserUsers = Collections.synchronizedSet(
		mutableSetOf<UUID>()
	)
	val inventoryTitleTransformers = mutableListOf<InventoryTitleTransformer>()

	override fun softEnable() {
		super.softEnable()

		// Disabled because we also want to block Bedrock cheats
		// NCPHookManager.addHook(CheckType.ALL, ncpHook)

		val protocolManager = ProtocolLibrary.getProtocolManager()
		protocolManager.addPacketListener(BedrockPacketListener(this))

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()

		// NCPHookManager.removeHook(ncpHook)
	}

	fun registerInventoryTitleTransformer(
		plugin: Plugin,
		matchInventory: (Component) -> (Boolean),
		newInventoryName: (Component) -> (Component)
	) {
		inventoryTitleTransformers.add(
			InventoryTitleTransformer(
				plugin,
				matchInventory,
				newInventoryName
			)
		)
	}

	@EventHandler
	fun onDisable(event: PluginDisableEvent) {
		inventoryTitleTransformers.removeIf { it.plugin == event.plugin }
	}

	@EventHandler
	fun onSocketListener(event: SocketReceivedEvent) {
		val obj = event.json.obj
		val type = obj["type"].nullString

		val removeFromList = type == "removeFromGeyserPlayerList"
		val addToList = type == "addToGeyserPlayerList"
		val uniqueId = obj["uniqueId"].nullString ?: return

		val uuid = UUID.fromString(uniqueId)
		if (addToList) {
			geyserUsers.add(uuid)
		} else if (removeFromList) {
			geyserUsers.remove(uuid)
		}
	}

	data class InventoryTitleTransformer(
		val plugin: Plugin,
		val matchInventory: (Component) -> (Boolean),
		val newInventoryName: (Component) -> (Component)
	)
}