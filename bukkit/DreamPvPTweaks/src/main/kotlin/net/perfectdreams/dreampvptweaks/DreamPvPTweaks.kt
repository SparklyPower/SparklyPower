package net.perfectdreams.dreampvptweaks

import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.schedule
import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRiptideEvent
import org.bukkit.util.Vector
import java.util.*

class DreamPvPTweaks : KotlinPlugin(), Listener {
	companion object {
		private val ENABLED_WORLDS = listOf(
			"RealArenasPvP",
			"SuperSuperTheEnd"
		)

		private val ENABLED_COMMANDS = listOf(
			"pc",
			"petcall",
			"tell",
			"r",
			".",
			"eventos",
			"kdr"
		)
	}

	override fun softEnable() {
		registerEvents(this)
	}

	val lastDamage = WeakHashMap<Player, Long>()
	val battleModeTasks = WeakHashMap<Player, BukkitSchedulerController>()

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		// Do not allow commands while you are in PvP
		if (!battleModeTasks.containsKey(e.player))
			return

		val command = e.message.removePrefix("/").split(" ")
			.firstOrNull()

		if (command !in ENABLED_COMMANDS) {
			e.isCancelled = true
			e.player.sendMessage("§cVocê não pode usar comandos durante o PvP!")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDamageParticles(e: EntityDamageByEntityEvent) {
		val victim = e.entity
		val materialData = when (victim) {
			is Skeleton -> Material.BONE_BLOCK.createBlockData()
			is Ghast -> Material.BONE_BLOCK.createBlockData()
			is MagmaCube -> Material.LAVA.createBlockData()
			is Blaze -> Material.LAVA.createBlockData()
			is Creeper -> Material.GREEN_WOOL.createBlockData()
			is Slime -> Material.GREEN_WOOL.createBlockData()
			is Enderman -> Material.BLACK_WOOL.createBlockData()
			is EnderDragon -> Material.BLACK_WOOL.createBlockData()
			is WitherSkeleton -> Material.BLACK_WOOL.createBlockData()
			is Wither -> Material.BLACK_WOOL.createBlockData()
			else -> Material.NETHER_WART.createBlockData()
		}

		victim.world.spawnParticle(
			Particle.BLOCK_CRACK,
			victim.location.add(0.0, 0.5, 0.0),
			(e.finalDamage * 40).toInt(),
			0.0,
			0.0,
			0.0,
			materialData
		)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDamage(e: EntityDamageByEntityEvent) {
		val victim = e.entity
		var damager = e.damager

		if (victim.world.name !in ENABLED_WORLDS) // Do not enable the PvP tweaks outside of the PvP worlds
			return

		// If the damager was a projectile, get the shooter
		if (damager is Projectile) {
			val shooter = damager.shooter
			if (shooter != null && shooter is Entity)
				damager = shooter
		}
		
		if (victim is Player && damager is Player) {
			val lastDamageReceivedVictim = lastDamage.getOrDefault(victim, 0L)
			val lastDamageReceivedDamager = lastDamage.getOrDefault(damager, 0L)

			if (System.currentTimeMillis() - lastDamageReceivedVictim >= 10000) {
				victim.sendMessage("§cVocê encontrou no modo de batalha! Você não poderá sair da arena até ele acabar!")
			}
			if (System.currentTimeMillis() - lastDamageReceivedDamager >= 10000) {
				damager.sendMessage("§cVocê encontrou no modo de batalha! Você não poderá sair da arena até ele acabar!")
			}

			lastDamage[damager] = System.currentTimeMillis()
			lastDamage[victim] = System.currentTimeMillis()

			createBattleModeTask(damager)
			createBattleModeTask(victim)
		}

		val materialData = when (victim) {
			is Skeleton -> Bukkit.createBlockData(Material.BONE_BLOCK)
			is Ghast -> Bukkit.createBlockData(Material.BONE_BLOCK)
			is MagmaCube -> Bukkit.createBlockData(Material.LAVA)
			is Blaze -> Bukkit.createBlockData(Material.LAVA)
			is Creeper -> Bukkit.createBlockData(Material.GREEN_WOOL)
			is Slime -> Bukkit.createBlockData(Material.GREEN_WOOL)
			is Enderman -> Bukkit.createBlockData(Material.BLACK_WOOL)
			is EnderDragon -> Bukkit.createBlockData(Material.BLACK_WOOL)
			is WitherSkeleton -> Bukkit.createBlockData(Material.BLACK_WOOL)
			is Wither -> Bukkit.createBlockData(Material.BLACK_WOOL)
			else -> Bukkit.createBlockData(Material.NETHER_WART)
		}

		victim.world.spawnParticle(
			Particle.BLOCK_CRACK,
			victim.location.add(0.0, 0.5, 0.0),
			(e.finalDamage * 40).toInt(),
			0.0,
			0.0,
			0.0,
			materialData
		)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onElytraToggle(e: EntityToggleGlideEvent) {
		e.isCancelled = e.entity.world.name == "RealArenasPvP"
	}

	fun createBattleModeTask(player: Player) {
		battleModeTasks[player]?.currentTask?.cancel()

		scheduler().schedule(this) {
			battleModeTasks[player] = this
			for (seconds in 0 until 10) {
				if (!battleModeTasks.containsKey(player) || battleModeTasks[player]?.currentTask?.isCancelled == true) {
					return@schedule
				}
				var compile = "§f["
				var compileSeconds: Int
				compileSeconds = 0
				while (seconds > compileSeconds) {
					compile = "$compile§a\u2588"
					++compileSeconds
				}
				while (10 > compileSeconds) {
					compile = "$compile§c\u2588"
					++compileSeconds
				}
				compile = "$compile§f]"
				player.sendActionBar("Modo Batalha " + compile + " §e" + seconds + "s")
				waitFor(20)
			}
			battleModeTasks.remove(player)

			player.sendActionBar("§aVocê saiu do modo de batalha!")
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		if (10_000L >= System.currentTimeMillis() - lastDamage.getOrDefault(e.player, 0L)) { // 10 seconds
			if (e.player.world.name in ENABLED_WORLDS) // Only kill the player inside of the PvP worlds
				e.player.damage(99999.9) // no u
		}

		lastDamage.remove(e.player)
		battleModeTasks.remove(e.player)
	}

	@EventHandler
	fun onRiptide(e: PlayerRiptideEvent) {
		if (e.player.world.name in ENABLED_WORLDS) {
			e.player.sendMessage("§cVocê não pode usar o encantamento de Correnteza na Arena PvP!")

			// Attempt to reset the player's riptide
			// This is very hacky since
			val originalLocation = e.player.location
			launchMainThread {
				repeat(20) {
					e.player.velocity = Vector(0, 0, 0)
					e.player.teleport(originalLocation)
					delay(1L)
				}
			}
		}
	}
}