package net.perfectdreams.sparklydreamer

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.sparklydreamer.utils.APIServer
import org.bukkit.event.Listener
import java.io.StringWriter

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