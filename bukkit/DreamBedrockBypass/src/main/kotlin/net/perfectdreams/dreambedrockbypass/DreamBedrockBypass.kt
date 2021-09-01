package net.perfectdreams.dreambedrockbypass

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import fr.neatmonster.nocheatplus.checks.CheckType
import fr.neatmonster.nocheatplus.hooks.NCPHookManager
import net.perfectdreams.dreamcore.listeners.SocketListener
import net.perfectdreams.dreamcore.network.socket.SocketReceivedEvent
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class DreamBedrockBypass : KotlinPlugin(), Listener {
	val ncpHook = Hook(this)
	val geyserUsers = Collections.synchronizedSet(
		mutableSetOf<UUID>()
	)

	override fun softEnable() {
		super.softEnable()

		NCPHookManager.addHook(CheckType.ALL, ncpHook)
	}

	override fun softDisable() {
		super.softDisable()

		NCPHookManager.removeHook(ncpHook)
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
}