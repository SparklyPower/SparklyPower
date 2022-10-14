package net.perfectdreams.dreamclubes

import net.perfectdreams.dreamclubes.commands.declarations.ClubeChatCommand
import net.perfectdreams.dreamclubes.commands.declarations.ClubesCommand
import net.perfectdreams.dreamclubes.commands.declarations.KDRCommand
import net.perfectdreams.dreamclubes.listeners.DeathListener
import net.perfectdreams.dreamclubes.tables.*
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamClubes : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§e§lCl§6§lube§8]"
	}
	val pendingInvites = mutableMapOf<UUID, Long>()

	override fun softEnable() {
		super.softEnable()

		registerCommand(ClubesCommand(this))
		registerCommand(ClubeChatCommand(this))
		registerCommand(KDRCommand(this))

		registerEvents(DeathListener(this))

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Clubes,
				ClubeMembers,
				ClubesHomes,
				ClubeHomeUpgrades,
				PlayerDeaths
			)
		}
	}
}