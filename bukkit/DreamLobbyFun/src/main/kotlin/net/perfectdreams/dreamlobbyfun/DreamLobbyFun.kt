package net.perfectdreams.dreamlobbyfun

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import com.xxmicloxx.NoteBlockAPI.NBSDecoder
import com.xxmicloxx.NoteBlockAPI.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.Song
import com.xxmicloxx.NoteBlockAPI.SoundCategory
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import net.perfectdreams.dreamauth.DreamAuth
import net.perfectdreams.dreamauth.utils.PlayerStatus
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamlobbyfun.commands.ConfigureServerCommand
import net.perfectdreams.dreamlobbyfun.commands.LobbyBypassCommand
import net.perfectdreams.dreamlobbyfun.dao.PlayerSettings
import net.perfectdreams.dreamlobbyfun.listeners.*
import net.perfectdreams.dreamlobbyfun.streamgame.GameEntitiesSnapshot
import net.perfectdreams.dreamlobbyfun.streamgame.GameState
import net.perfectdreams.dreamlobbyfun.streamgame.entities.LorittaPlayer
import net.perfectdreams.dreamlobbyfun.streamgame.entities.PlayerMovementState
import net.perfectdreams.dreamlobbyfun.tables.UserSettings
import net.perfectdreams.dreamlobbyfun.utils.ServerCitizen
import net.perfectdreams.dreamlobbyfun.utils.ServerCitizenData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapPalette
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class DreamLobbyFun : KotlinPlugin(), Listener {
	companion object {
		const val ITEM_INFO_KEY = "LobbyItemInfo"
		val SERVER_ONLINE_COUNT = ConcurrentHashMap<String, Int>()
		const val RENDER_FPS = 15
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
	lateinit var holographicDisplaysAPI: HolographicDisplaysAPI
	val lobbyImage = BufferedImage(8 * 128, 5 * 128, BufferedImage.TYPE_INT_RGB)
	val lobbyImageGraphics = lobbyImage.createGraphics()
		.apply {
			setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON
			)
		}

	private val fanArtArtistBox = ImageIO.read(File(dataFolder, "fan_art_artist_box.png"))
	private val latoBlackFont = Font.createFont(Font.TRUETYPE_FONT, File(dataFolder, "lato_black.ttf"))
	private val lorittaSimulator = GameState(this)
	val previousPacketMapDataById = ConcurrentHashMap<Int, ByteArray>()
	private val fanArtImages = listOf(
		FanArtImage(
			"yafyr",
			ImageIO.read(File(dataFolder, "images/lori-pantufa-dab.png"))
		),
		FanArtImage(
			"CookiLuck",
			ImageIO.read(File(dataFolder, "images/lori-cookiluck.png"))
		),
		FanArtImage(
			"Allouette",
			ImageIO.read(File(dataFolder, "images/lori-allouette.png"))
		),
		FanArtImage(
			"torta de caramelo-canela",
			ImageIO.read(File(dataFolder, "images/lori-torta.png"))
		),
		FanArtImage(
			"Myahzinha",
			ImageIO.read(File(dataFolder, "images/lori-myah-2.png"))
		),
		FanArtImage(
			"PankekoHanako",
			ImageIO.read(File(dataFolder, "images/lori-pankeko.png"))
		),
		FanArtImage(
			"Allouette",
			ImageIO.read(File(dataFolder, "images/lori-allouette-2.png"))
		),
		FanArtImage(
			"sleepy",
			ImageIO.read(File(dataFolder, "images/pantufa-sleepy.png"))
		),
		FanArtImage(
			"Myahzinha",
			ImageIO.read(File(dataFolder, "images/lori-myah.png"))
		),
		FanArtImage(
			"CookiLuck",
			ImageIO.read(File(dataFolder, "images/lori-fofoca.png"))
		),
		FanArtImage(
			"sugar",
			ImageIO.read(File(dataFolder, "images/lori-sugar.png"))
		),
		FanArtImage(
			"sleepy",
			ImageIO.read(File(dataFolder, "images/pantufa-sleepy-2.png"))
		),
	)

	val playersWithinTheMapRegion = ConcurrentHashMap.newKeySet<Player>()

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

	override fun softEnable() {
		super.softEnable()

		lorittaSimulator.start()
		holographicDisplaysAPI = HolographicDisplaysAPI.get(this)

		launchAsyncThread {
			var previousStateSnapshotCached: GameEntitiesSnapshot? = null
			var nextStateSnapshotCached: GameEntitiesSnapshot? = null
			var frame = 0

			while (true) {
				val startFrame = System.currentTimeMillis()

				val previousStateSnapshot = previousStateSnapshotCached ?: lorittaSimulator.snapshot
				val nextStateSnapshot = nextStateSnapshotCached ?: lorittaSimulator.snapshot

				if (previousStateSnapshot == null || nextStateSnapshot == null) {
					// No cached state present! Waiting until a snapshot is present...
					delay(100)
					continue
				}

				// println("Updating screen")
				val now = System.currentTimeMillis()
				val diff = now - nextStateSnapshot.takenAt
				val interpolatedPercentage = (diff / GameState.TICK_DURATION.toDouble())
					.coerceIn(0.0..1.0)
				// println("Current interpolation percentage is $interpolatedPercentage")

				// Wait 30s before changing the fan art
				val fanArtImageData = fanArtImages[(frame / (30 * RENDER_FPS)) % fanArtImages.size]

				lobbyImageGraphics.clearRect(0, 0, lobbyImage.width, lobbyImage.height)
				lobbyImageGraphics.drawImage(fanArtImageData.image, 0, 0, null)
				// Debug frame count
				// lobbyImageGraphics.color = Color.YELLOW
				// lobbyImageGraphics.drawString("Frame $frame", 25, 25)
				lobbyImageGraphics.color = Color.RED

				// Fan Art Artist Info Box
				val middle = lobbyImage.width / 2

				lobbyImageGraphics.font = latoBlackFont.deriveFont(18f)

				val str = "Fan Art feita por ${fanArtImageData.artist}"
				val strWidth = lobbyImageGraphics.fontMetrics.stringWidth(str)

				// Left
				lobbyImageGraphics.drawImage(
					fanArtArtistBox.getSubimage(0, 10, 6, 31),
					middle - (strWidth / 2) - 6,
					0,
					null
				)

				// Middle
				lobbyImageGraphics.drawImage(
					fanArtArtistBox.getSubimage(6, 10, 1, 31),
					middle - (strWidth / 2),
					0,
					strWidth,
					31,
					null
				)

				// Right
				lobbyImageGraphics.drawImage(
					fanArtArtistBox.getSubimage(7, 10, 6, 31),
					middle + (strWidth / 2),
					0,
					null
				)

				lobbyImageGraphics.color = Color.WHITE
				lobbyImageGraphics.drawString(
					str,
					middle - (strWidth / 2),
					22
				)

				fun lerp(x: Double, y: Double, t: Double): Double {
					return x*(1-t)+y*t
				}

				for ((entityId, lastEntitySnapshot) in previousStateSnapshot.entities) {
					val currentSnapshot = nextStateSnapshot.entities[entityId]

					if (currentSnapshot != null) {
						val interpolatedX = lerp(lastEntitySnapshot.x.toDouble(), currentSnapshot.x.toDouble(), interpolatedPercentage)
						val interpolatedY = lerp(lastEntitySnapshot.y.toDouble(), currentSnapshot.y.toDouble(), interpolatedPercentage)

						val sprite = when (lastEntitySnapshot.movementState) {
							is PlayerMovementState.FallingState -> when (lastEntitySnapshot.type) {
								LorittaPlayer.PlayerType.LORITTA -> lorittaSimulator.textures.lorittaHurtTexture
								LorittaPlayer.PlayerType.PANTUFA -> lorittaSimulator.textures.pantufaHurtTexture
								LorittaPlayer.PlayerType.GABRIELA -> lorittaSimulator.textures.gabrielaHurtTexture
							}
							is PlayerMovementState.IdleState -> when (lastEntitySnapshot.type) {
								LorittaPlayer.PlayerType.LORITTA -> lorittaSimulator.textures.lorittaIdleTexture
								LorittaPlayer.PlayerType.PANTUFA -> lorittaSimulator.textures.pantufaIdleTexture
								LorittaPlayer.PlayerType.GABRIELA -> lorittaSimulator.textures.gabrielaIdleTexture
							}
							is PlayerMovementState.JumpingState -> when (lastEntitySnapshot.type) {
								LorittaPlayer.PlayerType.LORITTA -> lorittaSimulator.textures.lorittaJumpingTexture
								LorittaPlayer.PlayerType.PANTUFA -> lorittaSimulator.textures.pantufaJumpingTexture
								LorittaPlayer.PlayerType.GABRIELA -> lorittaSimulator.textures.gabrielaJumpingTexture
							}
							is PlayerMovementState.RunningState -> {
								val frameIndex = ((lorittaSimulator.elapsedTicks / 10) % 6)

								val list = when (lastEntitySnapshot.type) {
									LorittaPlayer.PlayerType.LORITTA -> lorittaSimulator.textures.lorittaRunningTextures
									LorittaPlayer.PlayerType.PANTUFA -> lorittaSimulator.textures.pantufaRunningTextures
									LorittaPlayer.PlayerType.GABRIELA -> lorittaSimulator.textures.gabrielaRunningTextures
								}
								list[frameIndex]
							}
							is PlayerMovementState.HurtState -> when (lastEntitySnapshot.type) {
								LorittaPlayer.PlayerType.LORITTA -> lorittaSimulator.textures.lorittaHurtTexture
								LorittaPlayer.PlayerType.PANTUFA -> lorittaSimulator.textures.pantufaHurtTexture
								LorittaPlayer.PlayerType.GABRIELA -> lorittaSimulator.textures.gabrielaHurtTexture
							}
						}

						// println("Entity $entityId")
						// println("last x: ${lastEntitySnapshot.x}; last y: ${lastEntitySnapshot.y}")
						// println("current x: ${currentSnapshot.x}; current y: ${currentSnapshot.y}")
						// println("interpolatedX: $interpolatedX; interpolatedY: $interpolatedY")
						// The anchor point is 0.5, 0.5
						val mirror = lastEntitySnapshot.speed > 0
						lobbyImageGraphics.drawImage(
							sprite,
							if (mirror) interpolatedX.toInt() - 64 else interpolatedX.toInt() - 64 + 128,
							interpolatedY.toInt() - 128,
							if (mirror) 128 else -128,
							128,
							null
						)
					}
				}

				// Check if a new tick snapshot has arrived
				val nextNextStateSnapshot = lorittaSimulator.snapshot
				if (nextNextStateSnapshot != null && nextNextStateSnapshot.tick != nextStateSnapshot.tick) {
					// If yes, we will set the nextStateSnapshot to the previousStateSnapshot
					// and the nextNextStateSnapshot will be the new nextStateSnapshotCached
					previousStateSnapshotCached = nextStateSnapshot
					nextStateSnapshotCached = nextNextStateSnapshot
				}

				val packets = mutableListOf<ClientboundMapItemDataPacket>()

				var mapId = 1
				var bytesCount = 0

				for (y in 0 until 5) {
					for (x in 0 until 8) {
						val oldData = previousPacketMapDataById[mapId]
						val newData = MapPalette.imageToBytes(lobbyImage.getSubimage(x * 128, y * 128, 128, 128))

						var mapStartX = 0
						var mapStartZ = 0
						var mapWidth = 128
						var mapHeight = 128
						var mapData = newData

						// All pixels are laid out sequentially
						// We want to "crop" the image depending on what pixels were changed
						// https://stackoverflow.com/a/36938923/7271796
						if (oldData != null) {
							var top = 128 / 2
							var bottom = top
							var left = 128 / 2
							var right = left
							var differentContent = false

							for (i in oldData.indices) {
								val oldPixel = oldData[i]
								val newPixel = newData[i]

								// They are different!
								if (oldPixel != newPixel) {
									val pixelX = i % 128
									val pixelZ = i / 128

									top = top.coerceAtMost(pixelZ)
									bottom = bottom.coerceAtLeast(pixelZ)
									left = left.coerceAtMost(pixelX)
									right = right.coerceAtLeast(pixelX)
									differentContent = true
								}
							}

							if (!differentContent) {
								// Skipping map update: No new changes were made
								mapId++
								continue
							}

							// Create a new ByteArray based off the changed data
							// We can't use copyOfRange to copy the data directly from the original array (sad)
							mapStartX = left
							mapStartZ = top
							mapWidth = (right - left) + 1
							mapHeight = (bottom - top) + 1
							val onlyChangedPixelsData = ByteArray(mapWidth * mapHeight)

							var i = 0
							for (changedPixelZ in mapStartZ until (mapStartZ + mapHeight)) {
								for (changedPixelX in mapStartX until (mapStartX + mapWidth)) {
									onlyChangedPixelsData[i] = newData[changedPixelX + (changedPixelZ * 128)]
									i++
								}
							}

							mapData = onlyChangedPixelsData
						}

						if (mapData == null)
							error("Map data for $mapId is null! This should NEVER happen!")

						previousPacketMapDataById[mapId] = newData

						// println("Map $mapId Info - mapStartX: $mapStartX, mapStartZ: $mapStartZ, mapWidth: $mapWidth, mapHeight: $mapHeight, length: ${mapData.size}")
						packets.add(
							ClientboundMapItemDataPacket(
								mapId++,
								4,
								false,
								null,
								MapItemSavedData.MapPatch(
									mapStartX,
									mapStartZ,
									mapWidth,
									mapHeight,
									mapData
								)
							)
						)

						bytesCount += mapData.size
					}
				}

				// println("Bytes: $bytesCount")

				// Send packets to players
				// We use onlinePlayers instead of Bukkit.getOnlinePlayers() to avoid errors due to the async catcher
				// Don't schedule this on the main thread! Maybe cache the getOnlinePlayers somewhere but calling onMainThread here causes our render tick rate to drop to 20 ticks
				// (since we are synchronizing the call on the main thread)
				for (player in playersWithinTheMapRegion) {
					// println("Sending packet to ${player.name}")

					// Only update within the lobbymap_updater region, to avoid clients being bandwidth overloaded due to too many packets
					val craftPlayer = player as CraftPlayer
					for (packet in packets) {
						craftPlayer.handle
							.connection
							.send(packet)
					}
				}

				frame++
				val renderTime = System.currentTimeMillis() - startFrame
				// println("render time took $renderTime")

				delay((1_000 / RENDER_FPS) - renderTime)
			}
		}

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

		if (serverCitizensFile.exists()) {
			// Using ListSerializer(ServerCitizenData.serializer()) instead of <List<ServerCitizenData>> avoids plugin reload issues,
			// probably because <List<ServerCitizenData>> class reference is kept loaded somewhere?
			// I'm not really sure *what* causes it, because changing the code a bit seems to fix the issue???
			// But hey, it works here correctly so let's keep it that way
			val citizensData = Json.decodeFromString(ListSerializer(ServerCitizenData.serializer()), serverCitizensFile.readText())

			serverCitizens = citizensData.map {
				ServerCitizen(it, this)
			}.toMutableList()
		}

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

	override fun softDisable() {
		super.softDisable()

		serverCitizens.forEach {
			it.clickHereHologram?.delete()
			it.playerCountHologram?.delete()
			it.serverNameHologram?.delete()
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

	fun sendFullLobbyMap(player: Player) {
		for ((mapId, paletteData) in previousPacketMapDataById) {
			(player as CraftPlayer).handle
				.connection
				.send(
					ClientboundMapItemDataPacket(
						mapId,
						4,
						false,
						null,
						MapItemSavedData.MapPatch(
							0,
							0,
							128,
							128,
							paletteData
						)
					)
				)
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

	data class FanArtImage(
		val artist: String,
		val image: BufferedImage
	)
}