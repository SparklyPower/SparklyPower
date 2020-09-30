package net.perfectdreams.dreamhome

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamhome.commands.DelHomeCommand
import net.perfectdreams.dreamhome.commands.HomeCommand
import net.perfectdreams.dreamhome.commands.SetHomeCommand
import net.perfectdreams.dreamhome.dao.Home
import net.perfectdreams.dreamhome.listeners.ClaimCreateListener
import net.perfectdreams.dreamhome.tables.Homes
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamHome : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§e§lCasa§8]§e"
		val RANDOM_ICONS = mutableListOf(
				"https://i.imgur.com/bV2ytMB.png",
				"https://i.imgur.com/GtWxaK3.png",
				"https://i.imgur.com/cXrYx3G.png"
		)

		fun getRandomIcon(name: String): String {
			when {
				name.contains("casa") || name.contains("home") -> return "https://emojipedia-us.s3.amazonaws.com/thumbs/160/facebook/111/house-with-garden_1f3e1.png"
				name.contains("farm") || name.contains("fazenda") || name.contains("plantacao") || name.contains("plantação") || name.contains("plantaçao") || name.contains("plantacão") -> return "https://emojipedia-us.s3.amazonaws.com/thumbs/160/facebook/65/tractor_1f69c.png"
				name.contains("shop") || name.contains("loja") -> return "https://emojipedia-us.s3.amazonaws.com/thumbs/160/facebook/65/department-store_1f3ec.png"
				name.contains("pescar") || name.contains("pesca") || name.contains("peixe") || name.contains("fish") -> return "https://emojipedia-us.s3.amazonaws.com/thumbs/160/facebook/65/fishing-pole-and-fish_1f3a3.png"
				name.contains("spawner") || name.contains("mob") -> return "https://i.imgur.com/4CsYclN.png"
				name.contains("mina") || name.contains("mining") || name.contains("minerar") || name.contains("mine") -> return "https://emojipedia-us.s3.amazonaws.com/thumbs/160/facebook/65/fishing-pole-and-fish_1f3a3.png"
				name.contains("nether") -> return "https://i.imgur.com/lKK6kOA.png"
				name.contains("end") -> return "https://d1u5p3l4wpay3k.cloudfront.net/minecraft_gamepedia/0/06/End_Stone.png?version=5d07d460f5e6c428713dcfcb012fc785"
				name.contains("end") -> return "https://d1u5p3l4wpay3k.cloudfront.net/minecraft_gamepedia/0/06/End_Stone.png?version=5d07d460f5e6c428713dcfcb012fc785"
			}
			return RANDOM_ICONS[name.hashCode() % (RANDOM_ICONS.size - 1)]
		}

		/**
		 * Gets the max allowed homes for the [player]
		 */
		fun getMaxAllowedHomes(player: Player): Int {
			return when {
				player.hasPermission("dreamhome.houseplusplusplus") -> 20
				player.hasPermission("dreamhome.houseplusplus") -> 15
				player.hasPermission("dreamhome.houseplus") -> 10
				else -> 5
			}
		}
	}

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.create(Homes)
		}

		registerCommand(HomeCommand(this))
		registerCommand(SetHomeCommand(this))
		registerCommand(DelHomeCommand(this))
		registerEvents(ClaimCreateListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun loadHouses(player: Player, callback: (MutableList<Home>) -> Unit) = loadHouses(player.uniqueId, callback)

	fun loadHouses(player: UUID, callback: (MutableList<Home>) -> Unit) {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val houses = transaction(Databases.databaseNetwork) {
				Home.find { Homes.owner eq player }.toMutableList()
			}
			switchContext(SynchronizationContext.SYNC)
			callback.invoke(houses)
		}
	}

	fun createHouse(player: Player, _house: Home?, homeName: String, new: Boolean, oldLocation: Location?, newLocation: Location, callback: () -> Unit) {
		var house = _house
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			transaction(Databases.databaseNetwork) {
				if (house == null) {
					house = Home.new {
						owner = player.uniqueId
						houseName = homeName
						setLocation(newLocation)
						createdAt = System.currentTimeMillis()
						editedAt = System.currentTimeMillis()
					}
				} else {
					house!!.setLocation(newLocation)
					house!!.editedAt = System.currentTimeMillis()
				}
			}

			switchContext(SynchronizationContext.SYNC)
			val house = house ?: throw RuntimeException("home é nulo, mas isto jamais deve acontecer!")

			if (new) {
				Webhooks.PANTUFA_INFO?.send(
					DiscordMessage(
						content = "**${player.name}** marcou nova casa `${house.houseName}` em `${newLocation.world.name}` `${newLocation.x}`, `${newLocation.y}`, `${newLocation.z}`"
					)
				)
			} else {
				if (oldLocation != null)
					Webhooks.PANTUFA_INFO?.send(
						DiscordMessage(
							content = "**${player.name}** alterou a localização da casa `${house.houseName}`.\n\n**Localização Velha:** `${oldLocation.world.name}` `${oldLocation.x}`, `${oldLocation.y}`, `${oldLocation.z}`\n**Localização Nova:** `${newLocation.world.name}` `${newLocation.x}`, `${newLocation.y}`, `${newLocation.z}`"
						)
					)
			}

			callback.invoke()
		}
	}
}