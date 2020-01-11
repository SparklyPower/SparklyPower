package net.perfectdreams.dreamhome.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.Webhooks
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamhome.DreamHome
import net.perfectdreams.dreamhome.tables.Homes
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DelHomeCommand(val m: DreamHome) : SparklyCommand(arrayOf("delhome", "delcasa"), "dreamhome.delhome") {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage(generateCommandInfo(
				"delhome",
				mutableMapOf("NomeDaCasa" to "Nome da casa que você quer deletar"),
				listOf("Quer apenas mudar a localização de uma casa? Então use §6/sethome NomeDaCasa§7!")
		))
	}

	@Subcommand
	fun delHome(player: Player, name: String) {
		val homeName = name.toLowerCase()

		m.loadHouses(player) { houses ->
			val house = houses.firstOrNull { it.houseName.equals(homeName, true) }

			if (house == null) {
				player.sendMessage("§cCasa §e${homeName[0]}§c não existe!")
				return@loadHouses
			}

			houses.remove(house)

			scheduler().schedule(m, SynchronizationContext.ASYNC) {
				val newLocation = house.getLocation()
				transaction(Databases.databaseServer) {
					Homes.deleteWhere { Homes.id eq house.id }
				}

				switchContext(SynchronizationContext.SYNC)

				player.sendMessage("§aCasa deletada com sucesso!")

				Webhooks.PANTUFA_INFO.send(DiscordMessage(
						content = "**${player.name}** deletou casa `${house.houseName}`. **Localização:** `${newLocation.world.name}` `${newLocation.x}`, `${newLocation.y}`, `${newLocation.z}`"
				))
			}
		}
	}
}