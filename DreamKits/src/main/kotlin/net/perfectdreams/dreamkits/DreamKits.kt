package net.perfectdreams.dreamkits

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreamkits.commands.KitCommand
import net.perfectdreams.dreamkits.tables.Kits
import net.perfectdreams.dreamkits.utils.Kit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DreamKits : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§a§lKits§8] §e"
	}

	var kits = mutableListOf<Kit>()

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseServer) {
			SchemaUtils.create(Kits)
		}

		registerCommand(KitCommand(this))
		registerEvents(this)

		loadKits()
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		if (e.player.hasPlayedBefore())
			return

		kits.filter { it.giveOnFirstJoin }.forEach {
			giveKit(e.player, it)
		}
	}

	fun giveKit(player: Player, kit: Kit) {
		// We need to clone because DreamCorreios is dumb sometimes
		DreamCorreios.addItems(player.name, true, false, *kit.items.map { it.clone() }.toTypedArray())
	}

	fun loadKits() {
		val _kits = mutableListOf<Kit>()

		val folder = File(dataFolder, "kits/")

		for (file in folder.listFiles()) {
			_kits.add(DreamUtils.gson.fromJson(file.readText()))
		}

		kits = _kits
	}
}