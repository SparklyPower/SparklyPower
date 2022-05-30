package net.perfectdreams.dreamxizum

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamxizum.commands.DreamXizumCommand
import net.perfectdreams.dreamxizum.commands.XizumCommand
import net.perfectdreams.dreamxizum.listeners.XizumListener
import net.perfectdreams.dreamxizum.utils.ArenaXizum
import net.perfectdreams.dreamxizum.utils.RequestQueueEntry
import net.perfectdreams.dreamxizum.utils.WinType
import net.perfectdreams.dreamxizum.utils.XizumRequest
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.io.File

class DreamXizum : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§4§lXiz§c§lum§8]§e"
		const val XIZUM_DATA_KEY = "XizumData"
	}

	val arenas = mutableListOf<ArenaXizum>()
	val requestQueue = mutableListOf<RequestQueueEntry>()
	val requests = mutableListOf<XizumRequest>()
	val queue = mutableListOf<Player>()

	override fun softEnable() {
		super.softEnable()

		registerCommand(XizumCommand(this))
		registerCommand(DreamXizumCommand(this))

		registerEvents(XizumListener(this))

		loadArenas()
	}

	override fun softDisable() {
		super.softDisable()

		for (arena in arenas) {
			val p1 = arena.player1
			val p2 = arena.player2

			if (p1 != null && p2 != null) {
				arena.finishArena(p1, WinType.TIMEOUT)
			}
		}
	}

	fun addToQueue(player: Player) {
		queue.add(player)
		checkQueue()
	}

	fun addToRequestQueue(player1: Player, player2: Player) {
		requestQueue.add(RequestQueueEntry(player1, player2))
		checkQueue()
	}

	fun checkQueue() {
		while (queue.size >= 2) { // While there are two players in the queue...
			val queued1 = queue[0]
			val queued2 = queue[1]

			if (!queued1.isValid || !queued1.isOnline) {
				// Invalid, remove them from the queue
				queue.remove(queued1)
				continue
			}

			if (!queued2.isValid || !queued2.isOnline) {
				// Invalid, remove them from the queue
				queue.remove(queued2)
				continue
			}

			// We will now add them to our request queue!
			requestQueue.add(
				RequestQueueEntry(
					queued1,
					queued2
				)
			)

			// And remove them from our queue
			queue.remove(queued1)
			queue.remove(queued2)
		}
		while (requestQueue.isNotEmpty()) { // While there are request entries in the queue...
			val request = requestQueue[0]
			val queued1 = request.player1
			val queued2 = request.player2

			if (!queued1.isValid || !queued1.isOnline) {
				requestQueue.remove(request)
				queued2.sendMessage("§cVocê saiu da fila do Xizum pois o player que iria ir na partida com você saiu da fila...")
				continue
			}

			if (!queued2.isValid || !queued2.isOnline) {
				requestQueue.remove(request)
				queued1.sendMessage("§cVocê saiu da fila do Xizum pois o player que iria ir na partida com você saiu da fila...")
				continue
			}

			// Vamos pegar a primeira arena disponível para o nosso X1
			// Caso retorne null, cancele a verificação de queue já que não existem mais arenas disponíveis
			val arena = arenas.firstOrNull { it.data.isReady && it.player1 == null } ?: return

			requestQueue.remove(request)

			// omg todos são válidos??? yay??? não sei :X
			arena.startArena(queued1, queued2)
		}
	}

	fun saveArenas() {
		val arenasFolder = File(dataFolder, "arenas")

		arenasFolder.deleteRecursively()
		arenasFolder.mkdirs()

		arenas.forEach {
			File(arenasFolder, "${it.data.name}.json").writeText(Json.encodeToString(it.data))
		}
	}

	fun loadArenas() {
		val arenasFolder = File(dataFolder, "arenas")

		arenasFolder.listFiles().forEach {
			arenas.add(ArenaXizum(this, Json.decodeFromString(it.readText())))
		}
	}

	fun getArenaByName(name: String): ArenaXizum? {
		return arenas.firstOrNull { it.data.name == name }
	}
}