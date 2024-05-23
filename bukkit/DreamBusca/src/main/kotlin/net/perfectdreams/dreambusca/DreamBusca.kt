package net.perfectdreams.dreambusca

import kotlinx.coroutines.future.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.extensions.getSafeDestination
import net.perfectdreams.dreamcore.utils.extensions.worldGuardRegions
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.logging.Level
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DreamBusca : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§3§lBusca§8]"
		private const val FIVE_MINUTES_IN_TICKS = (5 * 60) * 20
	}

	val cachedLocations = mutableMapOf<BiomeMix, BiomeCache>()
	val survivalLocation by lazy {
		Location(Bukkit.getWorld("world"), 2503.5, 65.0, 257.5)
	}
	val teletransportationBox by lazy {
		Location(Bukkit.getWorld("world"), 2503.5, 241.0, 257.5)
	}

	val dataYaml by lazy {
		File(dataFolder, "data.yml")
	}

	val userData by lazy {
		if (!dataYaml.exists())
			dataYaml.writeText("")

		YamlConfiguration.loadConfiguration(dataYaml)
	}

	// Technically this doesn't need to be concurrent because we don't iterate on it
	val cachedInhabitedChunkTimers = mutableMapOf<String, BuscaWorldData>()

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

		for (biomeMix in BiomeMix.values()) {
			if (userData.contains(userData.name)) {
				val location = userData.getLocation("spawnLocation")
				if (location != null)
					cachedLocations[biomeMix] = BiomeCache(
						location,
						System.currentTimeMillis()
					)
			}
		}

		// Load all timers
		val buscaWorldDataFolder = File(dataFolder, "buscaworlddata")
		if (buscaWorldDataFolder.exists()) {
			buscaWorldDataFolder.listFiles().forEach {
				cachedInhabitedChunkTimers[it.nameWithoutExtension] = Json.decodeFromString(it.readText())
			}
		}
	}

	override fun softDisable() {
		super.softDisable()

		// Save all timers
		cachedInhabitedChunkTimers.forEach {
			File(dataFolder, "buscaworlddata/${it.key}.json")
				.writeText(Json.encodeToString(it.value))
		}
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEvent) {
		if (e.action != Action.PHYSICAL)
			return

		val clickedBlock = e.clickedBlock
		if (clickedBlock?.type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
			return

		val block = clickedBlock.getRelative(BlockFace.DOWN, 2)

		if (block.type != Material.OAK_SIGN && block.type != Material.OAK_WALL_SIGN)
			return

		val sign = block.state as Sign
		if (sign.getLine(0) != "[SparklyBusca]")
			return

		if (!e.player.location.worldGuardRegions.any { it.id.startsWith("survival") })
			return

		if (e.player.hasPotionEffect(PotionEffectType.INVISIBILITY))
			return

		val biomeMix = try {
			BiomeMix.valueOf(sign.getLine(1))
		} catch (exception: Exception) {
			logger.log(Level.SEVERE, "Erro ao processar selecionador de biomas em ${e.player.location}", exception)
			e.player.sendMessage("$PREFIX§c Algo deu errado! Pelo visto esse bioma tá dando ruim! Reporte para alguém da staff ok vlw")
			return
		}

		e.player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0))

		launchMainThread {
			// Pra que delay? Porque, se não tem, o console mostra "Moved wrongly!" e o player não muda de lugar
			// bad bad
			delayTicks(5L)

			e.player.sendMessage("$PREFIX §eBuscando bioma...")

			findBiome(e.player, biomeMix, {
				if (it == 0)
					e.player.teleport(teletransportationBox)

				val percentage = (it.toDouble() / (20 * 12))
				e.player.sendTitle("§bBuscando Bioma...", "§e${"%.2f".format(percentage * 100)}§6%", 0, 20, 10)
			}) {
				e.player.removePotionEffect(PotionEffectType.INVISIBILITY)

				if (it == null) {
					e.player.sendMessage("$PREFIX §cBioma não encontrado...")
					e.player.sendTitle("§cBioma não encontrado...", "§cNão tem um chunk carregado com o bioma desejado... sorry :(", 0, 100, 10)
					e.player.teleport(survivalLocation)
					return@findBiome
				}

				val location = try { it.getSafeDestination() } catch (exception: LocationUtils.HoleInFloorException) {
					e.player.sendMessage("$PREFIX §cBioma não encontrado...")
					e.player.sendTitle("§cBioma não encontrado...", "§cNão tem um chunk carregado com o bioma desejado... sorry :(", 0, 100, 10)
					e.player.teleport(survivalLocation)
					return@findBiome
				}

				e.player.sendTitle("§bBioma encontrado!", "", 0, 20, 10)
				e.player.teleport(location) // Teletransportar player
				e.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 600, 1)) // Efeito de velocidade
				e.player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 3)) // Efeito de anti dano (para evitar mortes)
			}
		}
	}

	fun findBiome(player: Player, biomeMix: BiomeMix, onChunkTrack: (Int) -> (Unit), locationCallback: (Location?) -> (Unit)) {
		if (biomeMix == BiomeMix.ALL_BIOMES && cachedLocations.isNotEmpty()) {
			// Se for todos os biomas, apenas retorne qualquer um do cache
			locationCallback.invoke(cachedLocations.values.random().location)
			return
		}

		val cachedLocation = cachedLocations[biomeMix]

		if (cachedLocation != null) {
			// cinco minutos
			if (300_000 >= System.currentTimeMillis() - cachedLocation.foundAt) {
				locationCallback.invoke(cachedLocation.location)
				return
			}
			// Iremos reutilizar depois, caso não tenhamos encontrado uma localização válida, mas existia no cache, mesmo que já tenha expirado.
		}

		val blacklistedX = 403.0 - 250.0..403.0 + 250.0
		val blacklistedZ = 257.0 - 250.0..257.0 + 250.0
		// Para procurar um bioma, iremos pegar primeiro os chunks carregados, para evitar IO desnecessário
		val world = player.world
		val chunk = world.loadedChunks.filter {
			// Se o bioma é do bioma desejado...
			val block = it.getBlock(0, 80, 0)
			val location = block.location

			// So, before we were checking if the player could break at the specified location (with "player.canBreakAt(...)")
			// But that was kinda bad because GriefPrevention causes a player lookup
			//
			// So, to avoid that, we will just check if there's *any* claims in the location and, if there is, we will just ignore
			//
			// Also, it makes sense: Why would the player want to teleport to a already claimed claim? (even if it is their own claim
			// https://cdn.discordapp.com/attachments/513405772911345664/815548215365206056/unknown.png
			biomeMix.isBiomeOfKind(block.biome) &&
					location.x !in blacklistedX &&
					location.z !in blacklistedZ &&
					location.world.worldBorder.isInside(location) &&
					!location.worldGuardRegions.any { it.id.contains("survival") } &&
					GriefPrevention.instance.dataStore.getClaimAt(location, false, null) == null
		}.run {
			if (this.isNotEmpty())
				this.random()
			else
				null
		}

		// Mas, se é nulo... Vamos ter que carregar chunks!
		launchMainThread {
			if (chunk != null) {
				val location = chunk.getBlock(0, 80, 0).location
				cachedLocations[biomeMix] = BiomeCache(location, System.currentTimeMillis())

				// uau, bioma novo!
				onAsyncThread {
					userData.set(biomeMix.name, location)
					userData.save(dataYaml)
				}

				locationCallback.invoke(location)
				return@launchMainThread
			}

			// O attention span de uma pessoa é + ou - 12 segundos
			// Se demorar mais de 12 segundos para encontrar o chunk, vamos desistir
			for (i in 0 until 20 * 12) {
				val location = Location(player.world, 403.0, 80.0, 257.0)
				// 403, 257 é o centro do spawn
				// Vamos procurar chunks além de 500 blocos
				val newX = DreamUtils.random.nextInt(403 + 500, 13_500).randomSign()
				val newZ = DreamUtils.random.nextInt(257 + 500, 13_500).randomSign()

				location.x = newX.toDouble()
				location.z = newZ.toDouble()

				if (biomeMix.isBiomeOfKind(location.block.biome) && !location.worldGuardRegions.any { it.id.contains("survival") } && player.canBreakAt(location, Material.DIRT)) {
					val location = location
					cachedLocations[biomeMix] = BiomeCache(location, System.currentTimeMillis())

					// uau, bioma novo!
					onAsyncThread {
						userData.set(biomeMix.name, location)
						userData.save(dataYaml)
					}

					locationCallback.invoke(location)
					return@launchMainThread
				}

				delayTicks(1L)

				onChunkTrack.invoke(i)
			}

			// Okay... não encontramos absolutamente NADA!
			// Mas antes de desistir... será que existia algum bioma deste tipo em nosso cache?
			if (cachedLocation != null) {
				// Se tinha, vamos usar ele e readicionar ao cache
				cachedLocations[biomeMix] = BiomeCache(cachedLocation.location, System.currentTimeMillis())
				locationCallback.invoke(cachedLocation.location)
				return@launchMainThread
			}

			// Se não... oh well, tentamos, né?
			locationCallback.invoke(null)
		}
	}

	private fun Int.randomSign(): Int {
		if (DreamUtils.random.nextBoolean())
			return this * -1
		return this
	}

	// This is from DreamResourceReset
	@EventHandler
	fun onInteract2(e: PlayerInteractEvent) {
		if (e.action != Action.PHYSICAL)
			return

		val clickedBlock = e.clickedBlock
		if (clickedBlock?.type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
			return

		if (e.player.world.name != "Survival2")
			return

		val block = clickedBlock.getRelative(BlockFace.DOWN, 2)

		if (block.type != Material.OAK_SIGN && block.type != Material.OAK_WALL_SIGN)
			return

		val sign = block.state as Sign
		if (sign.getLine(0) != "[SparklyBusca2]")
			return

		if (!e.player.location.worldGuardRegions.any { it.id.startsWith("spawn") })
			return

		if (e.player.hasPotionEffect(PotionEffectType.INVISIBILITY))
			return

		e.player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0))

		val world = e.player.world

		launchMainThread {
			var location: Location? = null

			// Pra que delay? Porque, se não tem, o console mostra "Moved wrongly!" e o player não muda de lugar
			// bad bad
			delayTicks(5L)

			e.player.sendMessage("$PREFIX §eBuscando lugar aleatório...")

			var chunksChecked = 0
			while (true) {
				try {
					// https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly
					val r = 7500 * sqrt(DreamUtils.random.nextDouble())
					val theta = DreamUtils.random.nextDouble() * 2 * Math.PI
					val x = (r * cos(theta)).toInt()
					val z = (r * sin(theta)).toInt()

					val chunkX = x shr 4
					val chunkY = z shr 4

					val inhabitedTimerInChunk = getInhabitedChunkTimerInResourcesWorldAt(world.name, chunkX, chunkY)

					logger.info { "Trying to use chunk ($x; $z = $chunkX, $chunkY); Inhabited timer in chunk is $inhabitedTimerInChunk" }

					// We are going to max check 60 chunks
					val bypassChecks = chunksChecked == 60

					if (!bypassChecks && inhabitedTimerInChunk >= FIVE_MINUTES_IN_TICKS) {
						logger.info { "Skipping Chunk ($x, $z) due to too much activeness! $inhabitedTimerInChunk >= $FIVE_MINUTES_IN_TICKS" }
						delayTicks(1L)
						chunksChecked++
						continue
					}

					// Get the chunk async to avoid synchronized loads
					val chunk = world.getChunkAtAsync(chunkX, chunkY).await()

					if (!bypassChecks) {
						// If there is any players in the current chunk, skip it
						if (chunk.entities.any { it is Player }) {
							logger.info { "Skipping Chunk ($x, $z) because there is another player in the same chunk!" }
							delayTicks(1L)
							chunksChecked++
							continue
						}

						val randomBlockInChunk = chunk.getBlock(0, 0, 0)
						if (randomBlockInChunk.biome == Biome.OCEAN || randomBlockInChunk.biome.name.endsWith("_OCEAN")) {
							logger.info { "Skipping Chunk ($x, $z) because it is an ocean!" }
							delayTicks(1L)
							chunksChecked++
							continue
						}
					}

					val highestY = world.getHighestBlockYAt(x, z)
					location = Location(world, x.toDouble(), highestY.toDouble(), z.toDouble()).getSafeDestination()
					break
				} catch (e: LocationUtils.HoleInFloorException) {
					delayTicks(1L)
					chunksChecked++
				}
			}

			if (location == null) {
				e.player.removePotionEffect(PotionEffectType.INVISIBILITY)
				return@launchMainThread
			}

			e.player.sendTitle("§bWoosh!", "", 0, 20, 10)
			e.player.teleportAsync(location).await() // Teletransportar player
			e.player.removePotionEffect(PotionEffectType.INVISIBILITY)
			// Efeito de velocidade
			e.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 600, 1))
			// Efeito de anti dano (para evitar mortes)
			e.player.addPotionEffect(
				PotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE,
					600,
					3
				)
			)
		}
	}

	@EventHandler
	fun onChunkLoad(e: ChunkLoadEvent) {
		if (e.world.name != "Survival2")
			return

		// Don't save if 0
		if (e.chunk.inhabitedTime == 0L)
			return

		// Save inhabited time on chunk load
		cachedInhabitedChunkTimers.getOrPut(e.chunk.world.name) { BuscaWorldData(mutableMapOf()) }.inhabitedChunkTimers[e.chunk.chunkKey] = e.chunk.inhabitedTime
	}

	@EventHandler
	fun onChunkUnload(e: ChunkUnloadEvent) {
		if (e.world.name != "Survival2")
			return

		// Don't save if 0
		if (e.chunk.inhabitedTime == 0L)
			return

		// Save inhabited time on chunk unload
		cachedInhabitedChunkTimers.getOrPut(e.chunk.world.name) { BuscaWorldData(mutableMapOf()) }.inhabitedChunkTimers[e.chunk.chunkKey] = e.chunk.inhabitedTime
	}

	/**
	 * Gets the inhabited chunk timer from the cache or, if it wasn't ever saved in the cache, null
	 */
	fun getInhabitedChunkTimerInResourcesWorldAt(worldName: String, x: Int, z: Int): Long {
		val world = Bukkit.getWorld(worldName)!!

		// If the chunk is loaded, load the inhabited time from memory
		if (world.isChunkLoaded(x, z))
			return world.getChunkAt(x, z).inhabitedTime

		// If not, load it from the cache
		val map = cachedInhabitedChunkTimers[worldName] ?: return 0
		return map.inhabitedChunkTimers.getOrDefault(Chunk.getChunkKey(x, z), 0L)
	}
}