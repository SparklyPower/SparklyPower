package net.perfectdreams.dreamhome.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.playTeleportEffects
import net.perfectdreams.dreamhome.DreamHome
import net.perfectdreams.dreamhome.dao.Home
import org.bukkit.entity.Player

class HomeCommand(val m: DreamHome) : SparklyCommand(arrayOf("home", "casa")) {
	/**
	 * Lists all [houses] to the specified [player]
	 */
	fun listHomes(player: Player, houses: List<Home>) {
		if (houses.isNotEmpty()) {
			val textComponent = "§3Casas (${houses.size}/${DreamHome.getMaxAllowedHomes(player)}): ".toTextComponent()
			var first = true
			for (house in houses) {
				if (!first) {
					textComponent += "§3, §b".toTextComponent()
				}
				textComponent += "§b${house.houseName}".toTextComponent().apply {
					hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, """§bX: §3${house.x}
									|§bY: §3${house.y}
									|§bZ: §3${house.z}
								""".trimMargin().toBaseComponent())
					clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home ${house.houseName}")
				}
				first = false
			}
			player.sendMessage(textComponent)

			player.sendMessage("")
			player.sendMessage("§3Visite uma casa usando §6/home NomeDaCasa")
			player.sendMessage("§3Delete uma casa usando §6/delhome NomeDaCasa")
		} else {
			player.sendMessage("§cVocê não tem nenhuma casa marcada! Para marcar uma, use §6/sethome NomeDaCasa")
		}
	}

	/**
	 * Teleports the [player] to the specified house [name], also checks if the location is on a valid location before teleporting.
	 */
	fun teleportToHome(player: Player, houses: List<Home>, name: String) {
		val house = houses.firstOrNull { it.houseName == name }
		if (house == null) {
			player.sendMessage("§cCasa §e${name}§c não existe!")
			return
		}

		val location = house.getLocation()

		if (location.blacklistedTeleport) {
			player.sendMessage("§cCasa §e${name}§c está marcada em um local proibido!")
			return
		}

		player.teleportAsync(location).thenRun {
			player.playTeleportEffects()
			player.sendMessage("§aVocê chegou ao seu destino. §cʕ•ᴥ•ʔ")
			player.sendTitle(
				"§b${house.houseName}",
				"§3${TextUtils.ROUND_TO_2_DECIMAL.format(location.x)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(
					location.y
				)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(location.z)}",
				10,
				60,
				10
			)
		}
	}

	@Subcommand
	fun root(player: Player) {
		m.loadHouses(player) { houses ->
			listHomes(player, houses)
		}
	}

	@Subcommand
	fun goToHome(player: Player, name: String) {
		m.loadHouses(player) { houses ->
			teleportToHome(player, houses, name)
		}
	}

	inner class LookupCommand : SparklyCommand(arrayOf("lookup"), permission = "dreamhome.staff") {
		@Subcommand
		fun goToPlayerHome(player: Player, playerName: String) {
			player.sendMessage("§dListando casas de $playerName...")

			m.loadHouses(playerName) { houses ->
				if (houses.isNotEmpty()) {
					val textComponent = "§3Casas de $playerName (${houses.size}): ".toTextComponent()
					var first = true
					for (house in houses) {
						if (!first) {
							textComponent += "§3, §b".toTextComponent()
						}
						textComponent += "§b${house.houseName}".toTextComponent().apply {
							hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, """§bX: §3${house.x}
									|§bY: §3${house.y}
									|§bZ: §3${house.z}
								""".trimMargin().toBaseComponent())
							clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home ${house.houseName}")
						}
						first = false
					}
					player.sendMessage(textComponent)

					player.sendMessage("")
					player.sendMessage("§3Visite uma casa usando §6/home NomeDaCasa")
					player.sendMessage("§3Delete uma casa usando §6/delhome NomeDaCasa")
				} else {
					player.sendMessage("§cVocê não tem nenhuma casa marcada! Para marcar uma, use §6/sethome NomeDaCasa")
				}
			}
		}

		@Subcommand
		fun goToPlayerHome(player: Player, playerName: String, name: String) {
			player.sendMessage("§dAcessando casas de $playerName...")

			m.loadHouses(playerName) { houses ->
				teleportToHome(player, houses, name)
			}
		}
	}
}
