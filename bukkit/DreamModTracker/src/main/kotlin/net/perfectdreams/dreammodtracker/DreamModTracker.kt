package net.perfectdreams.dreammodtracker

import net.perfectdreams.dreamcore.utils.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.*

class DreamModTracker : KotlinPlugin(), Listener, PluginMessageListener {
	private val registeredChannels = File(dataFolder, "registered-channels.log")

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		registerEvents(this)
	}

	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		logToConsoleAndFile(event.player.name + " (${event.player.uniqueId}) client brand name is ${event.player.clientBrandName}")
	}

	@EventHandler
	fun onPlayerRegisterChannel(event: PlayerRegisterChannelEvent) {
		logToConsoleAndFile(event.player.name + " (${event.player.uniqueId}) registered channel \"" + event.channel + "\"; client brand name is ${event.player.clientBrandName}")
	}

	// TODO: Implement this later
	override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {}

	private fun logToConsoleAndFile(message: String) {
		logger.info { message }
		logToFile(message)
	}

	private fun logToFile(message: String) {
		launchAsyncThread {
			val calendar = Calendar.getInstance()
			val date = "${String.format("%02d", calendar[Calendar.DAY_OF_MONTH])}/${
				String.format(
					"%02d",
					calendar[Calendar.MONTH] + 1
				)
			}/${String.format("%02d", calendar[Calendar.YEAR])} ${
				String.format(
					"%02d",
					calendar[Calendar.HOUR_OF_DAY]
				)
			}:${String.format("%02d", calendar[Calendar.MINUTE])}"

			registeredChannels.appendText("[$date] $message\n")
		}
	}
}