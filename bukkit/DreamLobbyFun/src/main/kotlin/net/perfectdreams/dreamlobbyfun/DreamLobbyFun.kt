package net.perfectdreams.dreamlobbyfun

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import com.xxmicloxx.NoteBlockAPI.NBSDecoder
import com.xxmicloxx.NoteBlockAPI.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.Song
import com.xxmicloxx.NoteBlockAPI.SoundCategory
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamlobbyfun.commands.ConfigureServerCommand
import net.perfectdreams.dreamlobbyfun.commands.LobbyBypassCommand
import net.perfectdreams.dreamlobbyfun.dao.PlayerSettings
import net.perfectdreams.dreamlobbyfun.listeners.*
import net.perfectdreams.dreamlobbyfun.tables.UserSettings
import net.perfectdreams.dreamlobbyfun.utils.ServerCitizen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DreamLobbyFun : KotlinPlugin(), Listener {
	companion object {
		const val ITEM_INFO_KEY = "LobbyItemInfo"
		val SERVER_ONLINE_COUNT = ConcurrentHashMap<String, Int>()
	}

	val launchPadDelay = WeakHashMap<Player, Long>()
	// Players que podem burlar coisas no servidor
	val unlockedPlayers = mutableListOf<Player>()
	var serverCitizens = mutableListOf<ServerCitizen>()

	val serverCitizensFile by lazy {
		dataFolder.mkdirs()
		File(dataFolder, "server_citizens_file.json")
	}
	val songsFolder by lazy {
		dataFolder.mkdirs()
		val songs = File(dataFolder, "songs")
		songs.mkdirs()
		songs
	}
	val songs = mutableListOf<Song>()
	var songPlayer: RadioSongPlayer? = null

	fun loadSongs() {
		songs.clear()
		songs.addAll(
				songsFolder.listFiles().filter {
					it.extension == "nbs"
				}.map {
					NBSDecoder.parse(it)
				}
		)
	}

	override fun onEnable() {
		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(UserSettings)
		}

		loadSongs()

		registerEvents(LaunchpadListener(this))
		registerEvents(FunPvPListener(this))
		registerEvents(SpawnListener(this))
		registerEvents(SimpleChatListener(this))
		registerEvents(TeleportBowListener(this))
		registerEvents(InventoryListener(this))
		registerEvents(ServerCitizenListener(this))
		registerEvents(SongListener(this))
		registerEvents(HungerListener(this))

		registerCommand(LobbyBypassCommand(this))
		registerCommand(ConfigureServerCommand(this))

		if (serverCitizensFile.exists())
			serverCitizens = DreamUtils.gson.fromJson(serverCitizensFile.readText())

		val newSongPlayer = RadioSongPlayer(songs.getRandom(), SoundCategory.RECORDS)
		logger.info("Tocando ${newSongPlayer.song.title}")
		newSongPlayer.autoDestroy = true
		newSongPlayer.isPlaying = true
		songPlayer = newSongPlayer

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				switchContext(SynchronizationContext.ASYNC)
				try {
					val jsonObject = JsonObject()
					jsonObject["type"] = "getOnlinePlayersInfo"

					val response = DreamNetwork.PERFECTDREAMS_BUNGEE.send(jsonObject)
					val servers = response["servers"].array

					servers.forEach {
						val obj = it.obj
						val name = obj["name"].string
						val playerCount = obj["players"].array.size()
						SERVER_ONLINE_COUNT[name] = playerCount
					}
				} catch (e: Exception) {
					plugin.slF4JLogger.error("Erro ao pegar informações de jogadores", e)
				}

				switchContext(SynchronizationContext.SYNC)
				serverCitizens.forEach {
					it.update()
				}
				waitFor(20 * 5)
			}
		}
	}

	fun giveLobbyItems(player: Player, playerInfo: PlayerSettings) {
		val inventory = player.inventory

		val cyanGlass = ItemStack(Material.CYAN_STAINED_GLASS_PANE, 1)
				.rename("§a")
		val lightBlueGlass = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1)
				.rename("§a")
		val blueGlass = ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1)
				.rename("§a")

		val playerVisibility = playerInfo.playerVisibility

		inventory.apply {
			this.clear()

			this.setItem(0,
					if (playerVisibility) {
						ItemStack(Material.LIME_DYE, 1).rename("§a§lPlayers estão visíveis")
								.lore("§7Cansado de aturar as outras pessoas?", "§7", "§7Então clique para deixar todas", "§7as outras pessoas invisíveis!")
								.storeMetadata(ITEM_INFO_KEY, "setPlayerVisibility:false")
					} else {
						ItemStack(Material.GRAY_DYE, 1, 8).rename("§c§lPlayers estão invisíveis")
								.lore("§7Está se sentindo sozinho?", "§7", "§7Então clique para deixar todas", "§7as outras pessoas visíveis!")
								.storeMetadata(ITEM_INFO_KEY, "setPlayerVisibility:true")
					}
			)

			this.setItem(4,
					ItemStack(Material.COMPASS).rename("§a§lSelecionador de Servidores")
							.lore("§7Clique para ver todos nossos servidores!")
							.storeMetadata(ITEM_INFO_KEY, "serverSelector")
			)

			this.setItem(8,
					ItemStack(Material.BOW).rename("§e§lArco Teletransportador")
							.lore("§7Com tédio?", "§7Esperando o seu servidor favorito reiniciar?", "§7", "§7Então que tal explorar o nosso", "§7lobby com este arco?")
							.apply {
								addEnchantment(Enchantment.ARROW_INFINITE, 1)
							}
			)

			this.setItem(1, cyanGlass)
			this.setItem(2, lightBlueGlass)
			this.setItem(3, cyanGlass)

			this.setItem(5, cyanGlass)
			this.setItem(6, lightBlueGlass)
			this.setItem(7, cyanGlass)

			for (i in 0..8) { // work smarter, not harder ᕕ(⌐■_■)ノ♪
				for (y in 0..2) {
					when (i % 4) {
						0 -> this.setItem(((y * 9) + 9) + i, blueGlass)
						1, 3 -> this.setItem(((y * 9) + 9) + i, cyanGlass)
						2 -> this.setItem(((y * 9) + 9) + i, lightBlueGlass)
					}
				}
			}

			this.setItem(17, ItemStack(Material.ARROW))

			this.heldItemSlot = 4
		}
	}

	fun getDreamAuthInstance() = Bukkit.getPluginManager().getPlugin("DreamAuth") as DreamAuth

	/**
	 * Teleports the player to the login location if they aren't logged in, this is used as a fail safe if the user, for some reason, was able to escape the login location without logging in!
	 *
	 * @param player the player that is going to be checked
	 * @return if the player wasn't logged in and they were teleported
	 */
	fun teleportToLoginLocationIfNotLoggedIn(player: Player): Boolean {
		val dreamAuth = getDreamAuthInstance()
		val playerStatus = dreamAuth.playerStatus[player]
		if (playerStatus != PlayerStatus.LOGGED_IN) {
			logger.warning { "Player $player is doing an action that they shouldn't be able to do because they aren't logged in! This means that there is a bug someone that must be fixed! We are going to teleport them to the login location..." }
			player.teleport(dreamAuth.authConfig.loginLocation ?: error("DreamAuth Login Location is not present!"))
			return true
		}
		return false
	}
}