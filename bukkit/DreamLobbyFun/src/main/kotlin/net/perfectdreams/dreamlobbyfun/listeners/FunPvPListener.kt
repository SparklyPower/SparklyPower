package net.perfectdreams.dreamlobbyfun.listeners

import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import net.perfectdreams.dreamlobbyfun.dao.PlayerSettings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class FunPvPListener(val m: DreamLobbyFun) : Listener {
	companion object {
		const val REGION_NAME = "fun_pvp_arena"
	}

	val lastDamage = WeakHashMap<Player, Long>()
	val battleModeTasks = WeakHashMap<Player, BukkitSchedulerController>()

	@EventHandler
	fun onMove(e: PlayerMoveEvent) {
		if (!e.displaced) // Vamos ignorar se o player não moveu
			return

		val player = e.player

		if (!e.from.isWithinRegion(REGION_NAME) && e.to.isWithinRegion(REGION_NAME)) { // Entrando na arena PvP
			player.inventory.clear()

			val diamondSword = ItemStack(Material.DIAMOND_SWORD)
			val diamondHelmet = ItemStack(Material.DIAMOND_HELMET)
			val diamondChestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
			val diamondLeggings = ItemStack(Material.DIAMOND_LEGGINGS)
			val diamondBoots = ItemStack(Material.DIAMOND_BOOTS)

			player.inventory.apply {
				this.setItem(EquipmentSlot.HAND, diamondSword)
				this.helmet = diamondHelmet
				this.chestplate = diamondChestplate
				this.leggings = diamondLeggings
				this.boots = diamondBoots
			}

			player.foodLevel = 20
			player.health = 20.0

			e.player.sendTitle("§aVocê entrou na Arena!", "§aDivirta-se!", 5, 40, 5)
		} else if (e.from.isWithinRegion(REGION_NAME) && !e.to.isWithinRegion(REGION_NAME)) { // Saindo da arena PvP
			val lastDamageReceived = lastDamage.getOrDefault(e.player, 0L)

			if (10000 > System.currentTimeMillis() - lastDamageReceived) {
				e.isCancelled = true
				player.sendMessage("§cVocê não pode sair da Arena PvP enquanto está em PvP!")
				e.player.sendTitle("§cVocê não pode sair!", "§cVocê está em modo PvP!", 5, 20, 5)
				return
			}

			player.inventory.clear()

			e.player.sendTitle("§cVocê saiu da Arena!", "", 5, 40, 5)

			player.foodLevel = 20
			player.health = 20.0

			scheduler().schedule(m, SynchronizationContext.ASYNC) {
				val playerInfo = transaction(Databases.databaseNetwork) {
					PlayerSettings.findById(player.uniqueId) ?: PlayerSettings.new(player.uniqueId) {
						playerVisibility = true
					}
				}

				switchContext(SynchronizationContext.SYNC)
				m.giveLobbyItems(player, playerInfo)
			}
		}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		lastDamage.remove(e.player)
		battleModeTasks.remove(e.player)
	}

	@EventHandler
	fun onDamage(e: EntityDamageEvent) {
		if (e.cause == EntityDamageEvent.DamageCause.FALL)
			e.isCancelled = true
	}

	// Isto é uma versão mais "compacta" do DreamPvPTweaks
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDamage(e: EntityDamageByEntityEvent) {
		val victim = e.entity
		val damager = e.damager

		if ((!victim.location.isWithinRegion(REGION_NAME) || !damager.location.isWithinRegion(REGION_NAME)) && !m.unlockedPlayers.contains(damager)) { // Desativar damage de entidades fora da arena
			e.isCancelled = true
			return
		}

		if (victim.location.isWithinRegion(REGION_NAME) && victim is Player && damager is Player) {
			val lastDamageReceivedVictim = lastDamage.getOrDefault(victim, 0L)
			val lastDamageReceivedDamager = lastDamage.getOrDefault(damager, 0L)
			if (System.currentTimeMillis() - lastDamageReceivedVictim >= 10000) {
				victim.sendMessage("§cVocê encontrou no modo de batalha! Você não poderá sair da arena até ele acabar!");
			}
			if (System.currentTimeMillis() - lastDamageReceivedDamager >= 10000) {
				damager.sendMessage("§cVocê encontrou no modo de batalha! Você não poderá sair da arena até ele acabar!");
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

	fun createBattleModeTask(player: Player) {
		battleModeTasks[player]?.currentTask?.cancel()

		scheduler().schedule(m) {
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
}