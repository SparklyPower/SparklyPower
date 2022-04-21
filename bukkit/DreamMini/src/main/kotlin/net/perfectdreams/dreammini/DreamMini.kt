package net.perfectdreams.dreammini

import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.hasStoredMetadataWithKey
import net.perfectdreams.dreammini.commands.*
import net.perfectdreams.dreammini.utils.*
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.Inventory
import java.io.File
import java.util.*

class DreamMini : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamMini
	}

	val joined = mutableSetOf<Player>()
	val left = mutableSetOf<Player>()
	var currentJoinSchedule: CoroutineTask? = null
	var currentLeftSchedule: CoroutineTask? = null
	var tpaManager = TpaManager()

	val dropsBlacklist = mutableMapOf<Player, Inventory>()
	val phantomWhitelist = mutableSetOf<UUID>()
	val weatherBlacklist = mutableSetOf<UUID>()

	val phantomFilterFile by lazy {
		File(dataFolder, "phantom-filter.txt")
	}
	val weatherBlacklistFile by lazy {
		File(dataFolder, "weather-filter.txt")
	}

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		if (config.getBoolean("fancy-unknown-command")) {
			registerEvents(UnknownCommandListener(this))
		}

		if (config.getBoolean("disable-phantoms")) {
			registerEvents(DisablePhantomsListener(this))
		}

		if (config.getBoolean("filter-phantoms")) {
			if (phantomFilterFile.exists()) {
				phantomWhitelist.addAll(
						phantomFilterFile.readLines().map { UUID.fromString(it) }
				)
			}

			registerEvents(FilterPhantomsListener(this))
			registerCommand(PhantomCommand(this))
		}

		if (config.getBoolean("filter-weather")) {
			if (weatherBlacklistFile.exists()) {
				weatherBlacklist.addAll(
						weatherBlacklistFile.readLines().map { UUID.fromString(it) }
				)
			}

			registerEvents(WeatherListener(this))
			registerCommand(ChuvaCommand(this))
		}

		if (config.getBoolean("nether-teleport")) {
			registerEvents(NetherTeleportListener(this))
		}

		if (config.getBoolean("atendente-da-loja")) {
			val listener = AtendenteListener(this)
			registerEvents(listener)
			registerCommand(AtendenteListener.AtendenteMenuCommand(listener))
			listener.loadAtendenteMenu()
		}

		if (config.getBoolean("back-to-spawn-on-unsafe-join")) {
			registerEvents(BackToSpawnListener(this))
		}

		registerEvents(this)
		registerEvents(DoNotPickupDropsListener(this))

		registerCommand(BroadcastCommand(this))
		registerCommand(FacebookCommand(this))
		registerCommand(SignEditCommand(this))
		registerCommand(SudoCommand(this))
		registerCommand(LixeiraCommand(this))
		registerCommand(DreamMiniCommand(this))

		registerCommand(TopCommand(this))
		registerCommand(SpeedCommand(this))
		registerCommand(FlyCommand(this))
		registerCommand(HatCommand(this))
		registerCommand(HealCommand(this))
		registerCommand(FeedCommand(this))
		registerCommand(SkullCommand(this))

		registerCommand(MemoryCommand(this))
		registerCommand(RenameCommand(this))
		registerCommand(LoreCommand(this))
		registerCommand(GameModeCommand(this))
		registerCommand(TpAllCommand(this))
		registerCommand(TpaCommand(this))
		registerCommand(TpaAquiCommand(this))
		registerCommand(TpaNegarCommand(this))
		registerCommand(TpaAceitarCommand(this))
		registerCommand(SpawnCommand(this))
		registerCommand(CalculatorCommand(this))
		registerCommand(OpenInvCommand(this))
		registerCommand(OpenEcCommand(this))
		registerCommand(DoNotPickupCommand(this))
		registerCommand(CraftCommand(this))
	}

	override fun softDisable() {
		super.softDisable()

		if (config.getBoolean("filter-phantoms")) {
			phantomFilterFile.writeText(
					phantomWhitelist.joinToString("\n", transform = { it.toString() })
			)
		}
		if (config.getBoolean("filter-weather")) {
			weatherBlacklistFile.writeText(
					weatherBlacklist.joinToString("\n", transform = { it.toString() })
			)
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onTeleport(e: PlayerTeleportEvent) =
		e.player.let {
			if (!it.hasPermission("sparklypower.soustaff")) return@let
			if (e.to.world == e.from.world) return@let
			if (!it.allowFlight) return@let

			schedule {
				waitFor(10L)
				it.allowFlight = true
				if (!it.isOnGround) it.isFlying = true
			}
		}

	// Needs to be veery low to avoid users using "&rSparklyShop" to bypass the Admin Shop check
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onEdit(e: SignChangeEvent) {
		if (e.player.hasPermission("dreammini.colorize")) {
			for (idx in 0..3) {
				val line = e.getLine(idx)
				if (line != null)
					e.setLine(idx, line.translateColorCodes())
			}
		}
	}

	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		if (e.player.hasPermission("dreammini.keepfly") && !e.player.isOnGround) { // Se o usuário deslogar enquanto está no ar, ative o fly
			scheduler().schedule(this) {
				waitFor(20)
				e.player.allowFlight = true
				e.player.isFlying = true
				e.player.sendMessage("§3Modo de vôo foi ativado automaticamente já que você não estava no chão ao sair, para desativar, use §6/fly")
			}
		}

		if (config.getBoolean("fancy-join", true)) {
			// Remover mensagem de entrada/saída
			e.joinMessage = null
			joined.add(e.player)
			left.remove(e.player)

			currentJoinSchedule?.cancel()

			val schedule = scheduler().schedule(this) {
				waitFor(20 * 7) // Esperar sete segundos
				val joinedPlayers = joined.filter { it.isOnline }

				if (joinedPlayers.isEmpty())
					return@schedule

				val aux = if (joinedPlayers.size == 1) {
					"entrou"
				} else {
					"entraram"
				}

				broadcast("§8[§a+§8] §b${joinedPlayers.joinToString("§a, §b", transform = { it.name })}§a $aux no jogo!")

				joined.clear()
			}

			currentJoinSchedule = schedule

			// Spawnar fireworks com cores aleatórias quando o player entrar no servidor
			val r = DreamUtils.random.nextInt(0, 256)
			val g = DreamUtils.random.nextInt(0, 256)
			val b = DreamUtils.random.nextInt(0, 256)

			val fadeR = Math.max(0, r - 60)
			val fadeG = Math.max(0, g - 60)
			val fadeB = Math.max(0, b - 60)

			val fireworkEffect = FireworkEffect.builder()
					.withTrail()
					.withColor(Color.fromRGB(r, g, b))
					.withFade(Color.fromRGB(fadeR, fadeG, fadeB))
					.with(FireworkEffect.Type.values()[DreamUtils.random.nextInt(0, FireworkEffect.Type.values().size)])
					.build()

			val firework = e.player.world.spawnEntity(e.player.location, EntityType.FIREWORK) as Firework
			val fireworkMeta = firework.fireworkMeta

			fireworkMeta.power = 1
			fireworkMeta.addEffect(fireworkEffect)

			firework.fireworkMeta = fireworkMeta
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		tpaManager.requests = tpaManager.requests.asSequence().filter { it.requestee != e.player || it.requester != e.player }.toMutableList()
		dropsBlacklist.remove(e.player)

		if (getConfig().getBoolean("fancy-quit", true)) {
			// Remover mensagem de entrada/saída
			e.quitMessage = null
			left.add(e.player)
			joined.remove(e.player)

			currentLeftSchedule?.cancel()

			val schedule = scheduler().schedule(this) {
				waitFor(20 * 7) // Esperar sete segundos
				val leftPlayers = left.filter { !it.isOnline }

				if (leftPlayers.isEmpty())
					return@schedule

				val aux = if (leftPlayers.size == 1) {
					"saiu"
				} else {
					"sairam"
				}

				broadcast("§8[§c-§8] §b${leftPlayers.joinToString("§c, §b", transform = { it.name })}§c $aux do jogo...")

				left.clear()
			}

			currentLeftSchedule = schedule
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onItemDrop(e: PlayerDropItemEvent) {
		if (config.getBoolean("special-lucky-drop-item", true)) {
			val type = e.itemDrop?.itemStack?.type
			val player = e.player

			if (type == Material.GOLD_NUGGET || type == Material.GOLD_INGOT || type == Material.GOLD_BLOCK) {
				scheduler().schedule(this) {
					waitFor(1)
					for (idx in 0 until 10) {
						if (!e.itemDrop.isValid)
							return@schedule

						val location = e.itemDrop.location

						if (location.block.type == Material.WATER) {
							if (WorldGuardUtils.isWithinRegion(location, "loja-sorte")) {
								var rewarded = false
								for (amount in 0 until e.itemDrop.itemStack.amount) {
									var chance = DreamUtils.random.nextInt(0, 101)

									// TODO: Prêmios
									if (chance == 100) {
										player.balance += 1750
										player.sendMessage("§aHoje é o seu dia de sorte!")
										rewarded = true
									}
								}
								if (rewarded) {
									e.itemDrop.location.world.spawnParticle(Particle.VILLAGER_HAPPY, e.itemDrop.location, 5, 0.5, 0.5, 0.5)
								} else {
									e.player.sendMessage("§cQue pena, pelo visto você não ganhou nada...")
									e.itemDrop.location.world.spawnParticle(Particle.VILLAGER_ANGRY, e.itemDrop.location, 5, 0.5, 0.5, 0.5)
								}
								e.itemDrop.remove()
								return@schedule
							}
						}
						waitFor(10)
					}
					return@schedule
				}
			}
		}
	}

	@EventHandler
	fun onCraft(e: CraftItemEvent) {
		for (item in e.inventory) {
			if (item != null && item.hasStoredMetadataWithKey("disallowCrafting"))
				e.isCancelled = true
		}
	}

	@EventHandler
	fun onDeath(e: PlayerDeathEvent) {
		if (config.getBoolean("disable-death-messages", true)) {
			e.deathMessage = null
		}
	}

	@EventHandler
	fun onSetFire (e: BlockIgniteEvent){
		val entity : Entity? = e.ignitingEntity
		if(e.cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && entity is Player) {
			entity.playSound(entity.location,"perfectdreams.sfx.fogobicho", 1F, 1F)
		}
	}
	@EventHandler
	fun FaleceuSfx (e: PlayerDeathEvent){
		val entity : Entity? = e.entity
		if (entity is Player) {
			entity.playSound(entity.location, "perfectdreams.sfx.faleceu", 1F, 1F)
		}
	}
}
