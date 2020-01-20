package net.perfectdreams.dreamraspadinha

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamraspadinha.commands.ScratchCardCommand
import net.perfectdreams.dreamraspadinha.listeners.InventoryListener
import net.perfectdreams.dreamraspadinha.tables.Raspadinhas
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DreamRaspadinha : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§a§lRaspadinha§8]"
	}

	override fun softEnable() {
		super.softEnable()

		registerCommand(ScratchCardCommand(this))
		registerEvents(InventoryListener(this))

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Raspadinhas
			)
		}
	}
}