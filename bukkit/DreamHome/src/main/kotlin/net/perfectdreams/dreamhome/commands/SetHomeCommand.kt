package net.perfectdreams.dreamhome.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamhome.DreamHome
import net.perfectdreams.dreamhome.dao.Home
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class SetHomeCommand(val m: DreamHome) : SparklyCommand(arrayOf("sethome", "setcasa"), permission = "dreamhome.sethome") {
	@Subcommand
	fun root(player: Player) {
		player.sendMessage(generateCommandInfo(
				"sethome",
				mutableMapOf("NomeDaCasa" to "Nome que identificará a sua casa"),
				listOf("Você pode ir para uma casa marcada a hora que você quiser! Só usar §6/home NomeDaCasa§7!")
		))
	}

	@Subcommand
	fun setHome(player: Player, name: String) {
		val homeName = name.toLowerCase()

		if (player.location.blacklistedTeleport) {
			player.sendMessage("§cVocê não pode marcar uma casa aqui!")
			return
		}

		m.loadHouses(player) { houses ->
			val house = houses.firstOrNull { it.houseName.equals(homeName, true) }

			val new = house == null
			val oldLocation = house?.getLocation()

			if (house == null) {
				if (!canCreateNew(player, houses)) {
					player.sendMessage("§cVocê chegou ao limite de casas! Para poder marcar novas, delete antigas usando §c/delhome NomeDaCasa")
					player.sendMessage("")
					player.sendMessage("§3Você também pode conseguir marcar mais casas comprando VIP! §6/vip")
					return@loadHouses
				}
			}

			val newLocation = player.location

			m.createHouse(
				player,
				house,
				homeName,
				new,
				oldLocation,
				newLocation
			) {
				if (new) {
					player.sendMessage("§aCasa marcada com sucesso! Que tal dar uma passadinha nela? §6/home $homeName")
				} else {
					player.sendMessage("§aCasa remarcada com sucesso!")
				}

				if (player.world.name == "Resources") {
					player.sendMessage("§c")
					player.sendMessage("§cVocê marcou o teletransporte rápido no mundo de recursos. O mundo de recursos é regenerado constantemente!")
					player.sendMessage("§cNão construa coisas importantes no mundo de recursos! Se você quer construir a sua humilde residência, construa")
					player.sendMessage("§cna §6/warp survival§c!")
					player.sendMessage("§cO seu teletransporte rápido será automaticamente deletado quando o mundo de recursos for resetado!")
				} else {
					val claim = GriefPrevention.instance.dataStore.getClaimAt(newLocation, false, null)

					if (claim == null) {
						player.sendMessage("§eParece que você ainda não protegeu o terreno aonde você marcou o teletransporte rápido! Para evitar griefings e roubos, use a pá de ouro do §6/kit noob§e para proteger!")
					}
				}
			}
		}
	}

	fun canCreateNew(player: Player, houses: List<Home>): Boolean {
		val maxAllowed = DreamHome.getMaxAllowedHomes(player)

		if (houses.size >= maxAllowed)
			return false

		return true
	}
}