package net.perfectdreams.sparklydreamer

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.sparklydreamer.utils.APIServer
import org.bukkit.event.Listener

class SparklyDreamer : KotlinPlugin(), Listener {
	var apiServer = APIServer(this)

	override fun softEnable() {
		super.softEnable()

		apiServer.start()
	}

	override fun softDisable() {
		super.softDisable()

		apiServer.stop()
	}
}