package net.perfectdreams.dreamwarps

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamwarps.commands.DreamWarpCommand
import net.perfectdreams.dreamwarps.commands.WarpCommand
import net.perfectdreams.dreamwarps.utils.Warp
import net.perfectdreams.dreamwarps.utils.WarpInventoryHolder
import net.perfectdreams.dreamwarps.utils.WarpMenu
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class DreamWarps : KotlinPlugin(), Listener {
	companion object {
		val PREFIX = "§8[§5§lGPS§8]§e"
	}

	var warps = mutableListOf<Warp>()

	lateinit var warpsMenu: WarpMenu

	fun loadWarpsMenu() {
		val menuFile = File(dataFolder, "warp_menu.kts")

		val source = menuFile.readText()

		println(source)

		val evaluated = DreamScriptManager.evaluate<Any>(this, """
			${Imports.IMPORTS}

			class CustomWarpsMenu : net.perfectdreams.dreamwarps.utils.WarpMenu {
				override fun generateMenu(player: Player): net.perfectdreams.dreamcore.utils.DreamMenu = ${menuFile.readText()}
			}

			CustomWarpsMenu()
		""".trimIndent())

		println(evaluated)

		if (evaluated is WarpMenu) {
			println("Sim, é uma instância de warp menu!")
		}

		warpsMenu = evaluated as WarpMenu
	}

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		loadConfig()
		loadWarpsMenu()

		registerCommand(WarpCommand(this))
		registerCommand(DreamWarpCommand(this))
		registerEvents(this)

		updateChunkTickets()
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun loadConfig() {
		warps.clear()
		reloadConfig()

		val warpsFolder = File(dataFolder, "warps")
		warpsFolder.mkdirs()

		warpsFolder.listFiles().forEach {
			if (it.extension == "json") {
				warps.add(DreamUtils.gson.fromJson(it.readText()))
			}
		}
	}

	fun saveWarps() {
		val warpsFolder = File(dataFolder, "warps")
		warpsFolder.deleteRecursively()
		warpsFolder.mkdirs()

		for (warp in warps) {
			File(warpsFolder, "${warp.name}.json").writeText(DreamUtils.gson.toJson(warp))
		}
	}

	@EventHandler
	fun onClick(e: InventoryClickEvent) {
		val holder = e.inventory?.holder ?: return

		if (holder !is WarpInventoryHolder)
			return

		e.isCancelled = true
		val player = e.whoClicked as Player
		player.closeInventory()

		val currentItem = e.currentItem ?: return
		if (currentItem.type == Material.AIR)
			return

		val data = e.currentItem?.getStoredMetadata("warpName") ?: return

		player.performCommand("dwarps $data")
	}

	fun updateChunkTickets() {
		val worlds = Bukkit.getWorlds()
		worlds.forEach {
			it.removePluginChunkTickets(this)
		}

		warps.forEach {
			it.location.chunk.addPluginChunkTicket(this)
		}
	}
}