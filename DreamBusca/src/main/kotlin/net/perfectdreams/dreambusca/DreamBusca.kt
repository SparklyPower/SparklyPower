package net.perfectdreams.dreambusca

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.extensions.getSafeDestination
import net.perfectdreams.dreamcore.utils.extensions.worldGuardRegions
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.logging.Level

class DreamBusca : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§3§lBusca§8]"
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
	}

	override fun softDisable() {
		super.softDisable()
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

		scheduler().schedule(this) {
			// Pra que delay? Porque, se não tem, o console mostra "Moved wrongly!" e o player não muda de lugar
			// bad bad
			waitFor(5L)

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

		val blacklistedX = 403 - 250..403 + 250
		val blacklistedZ = 257 - 250..257 + 250
		// Para procurar um bioma, iremos pegar primeiro os chunks carregados, para evitar IO desnecessário
		val world = player.world
		val chunk = world.loadedChunks.filter {
			// Se o bioma é do bioma desejado...
			val block = it.getBlock(0, 80, 0)
			val location = block.location
			biomeMix.isBiomeOfKind(block.biome) &&
					location.x !in blacklistedX &&
					location.z !in blacklistedZ &&
					!location.worldGuardRegions.any { it.id.contains("survival")} &&
					player.canBreakAt(location, Material.DIRT)
		}.run {
			if (this.isNotEmpty())
				this.random()
			else
				null
		}

		// Mas, se é nulo... Vamos ter que carregar chunks!
		scheduler().schedule(this) {
			if (chunk != null) {
				val location = chunk.getBlock(0, 80, 0).location
				cachedLocations[biomeMix] = BiomeCache(location, System.currentTimeMillis())

				// uau, bioma novo!
				switchContext(SynchronizationContext.ASYNC)
				userData.set(biomeMix.name, location)
				userData.save(dataYaml)
				switchContext(SynchronizationContext.SYNC)

				locationCallback.invoke(location)
				return@schedule
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
					switchContext(SynchronizationContext.ASYNC)
					userData.set(biomeMix.name, location)
					userData.save(dataYaml)
					switchContext(SynchronizationContext.SYNC)

					locationCallback.invoke(location)
					return@schedule
				}

				waitFor(1)

				onChunkTrack.invoke(i)
			}

			// Okay... não encontramos absolutamente NADA!
			// Mas antes de desistir... será que existia algum bioma deste tipo em nosso cache?
			if (cachedLocation != null) {
				// Se tinha, vamos usar ele e readicionar ao cache
				cachedLocations[biomeMix] = BiomeCache(cachedLocation.location, System.currentTimeMillis())
				locationCallback.invoke(cachedLocation.location)
				return@schedule
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
}