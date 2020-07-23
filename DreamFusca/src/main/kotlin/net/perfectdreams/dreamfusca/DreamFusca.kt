package net.perfectdreams.dreamfusca

import com.comphenix.protocol.ProtocolLibrary
import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamfusca.utils.CarHandlerPacketAdapter
import net.perfectdreams.dreamfusca.utils.CarInfo
import net.perfectdreams.dreamfusca.utils.CarType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

class DreamFusca : KotlinPlugin(), Listener {
	companion object {
		const val CAR_INFO_KEY = "DreamFusca"
		const val FUSCA_CHECK_KEY = "isFusca"
	}

	val blocks = listOf(
		Material.BLACK_CONCRETE,
		Material.STONE_SLAB,
		Material.BLACK_WOOL,
		Material.BLACK_TERRACOTTA,
		Material.COAL_BLOCK,
		Material.BLACK_CONCRETE_POWDER
	)

	var cars = mutableMapOf<UUID, CarInfo>()

	override fun softEnable() {
		super.softEnable()

		val file = File(dataFolder, "cars.json")

		if (file.exists()) {
			cars = DreamUtils.gson.fromJson(file.readText())
		}

		registerEvents(this)
		val protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(CarHandlerPacketAdapter(this))

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				waitFor(20 * 900)
				file.writeText(DreamUtils.gson.toJson(cars))
			}
		}

		registerCommand(command("DreamFuscaCommand", listOf("dreamfusca")) {
			permission = "dreamfusca.spawncarro"

			executes {
				player.sendMessage("§e/dreamfusca give")
			}
		})

		registerCommand(command("DreamFuscaCommand", listOf("dreamfusca give")) {
			permission = "dreamfusca.spawncarro"

			executes {
				var target: Player? = player

				args.getOrNull(0)?.let {
					target = Bukkit.getPlayer(it)
				}

				if (target == null) {
					player.sendMessage("§cPlayer inexistente!")
					return@executes
				}

				val carInfo = CarInfo(
					target!!.uniqueId,
					target!!.name,
					CarType.FUSCA
				)

				val itemStack = ItemStack(Material.MINECART)
					.rename("§3§lFusca")
					.lore("§7Fusca de §b${target?.name}")
					.storeMetadata(CAR_INFO_KEY, DreamUtils.gson.toJson(carInfo))
					.storeMetadata(FUSCA_CHECK_KEY, "true")

				player.inventory.addItem(itemStack)
			}
		})
	}

	override fun softDisable() {
		super.softDisable()
		val file = File(dataFolder, "cars.json")
		file.writeText(DreamUtils.gson.toJson(cars))
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onMount(e: VehicleEnterEvent) {
		if (e.vehicle.type != EntityType.MINECART)
			return

		if (!cars.containsKey(e.vehicle.uniqueId))
			return

		val carInfo = cars[e.vehicle.uniqueId]!!

		val owner = carInfo.owner

		if (e.entered.uniqueId != owner) {
			e.isCancelled = true
			e.entered.sendMessage("§cVocê não pode entrar no carro de §b${carInfo.owner}§c!")
			return
		}

		e.entered.sendMessage("§aVocê entrou no seu carro, não se esqueça de colocar o cinto de segurança e, é claro, se beber não dirija!")
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onExit(e: VehicleExitEvent) {
		if (e.vehicle.type != EntityType.MINECART)
			return

		if (!cars.containsKey(e.vehicle.uniqueId))
			return

		GriefPrevention.instance.dataStore.getClaimAt(e.vehicle.location, false, null)
			?: return

		// If inside a protected claim, kill the minecart and drop it

		val carInfo = cars[e.vehicle.uniqueId]!!

		val itemStack = ItemStack(Material.MINECART)
			.rename("§3§lFusca")
			.lore("§7Fusca de §b${carInfo.playerName}")
			.storeMetadata(CAR_INFO_KEY, DreamUtils.gson.toJson(carInfo))
			.storeMetadata(FUSCA_CHECK_KEY, "true")

		e.vehicle.world.dropItemNaturally(e.vehicle.location, itemStack)

		e.vehicle.remove()
	}

	@EventHandler
	fun onDestroy(e: VehicleDestroyEvent) {
		val attacker = e.attacker ?: return

		if (e.vehicle.type != EntityType.MINECART)
			return

		if (!cars.containsKey(e.vehicle.uniqueId))
			return

		val carInfo = cars[e.vehicle.uniqueId]!!

		if (carInfo.owner == attacker.uniqueId) {
			e.isCancelled = true
			e.vehicle.remove()

			val itemStack = ItemStack(Material.MINECART)
				.rename("§3§lFusca")
				.lore("§7Fusca de §b${carInfo.playerName}")
				.storeMetadata(CAR_INFO_KEY, DreamUtils.gson.toJson(carInfo))
				.storeMetadata(FUSCA_CHECK_KEY, "true")

			attacker.world.dropItemNaturally(e.vehicle.location, itemStack)
			cars.remove(e.vehicle.uniqueId)
		} else {
			if (!attacker.hasPermission("dreamfusca.overridecarbreak")) {
				e.isCancelled = true
				attacker.sendMessage("§cVocê não pode quebrar o carro de §b${carInfo.owner}§c!")
			} else {
				attacker.sendMessage("§7Você quebrou o carro de §b${carInfo.owner}§7!")
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onInteract(e: PlayerInteractEvent) {
		val item = e.item ?: return
		val clickedBlock = e.clickedBlock ?: return

		val type = item.type

		if (type != Material.MINECART)
			return

		val isFusca = item.getStoredMetadata(FUSCA_CHECK_KEY) ?: return

		val storedInfo = item.getStoredMetadata(CAR_INFO_KEY)?.let { DreamUtils.gson.fromJson<CarInfo>(it) }
			?: CarInfo(
				e.player.uniqueId,
				e.player.name,
				CarType.FUSCA
			)

		if (e.useItemInHand() == Event.Result.DENY && e.clickedBlock?.type !in blocks)
			return

		// Let players place cars anywhere, as long as it is in a valid block
		e.isCancelled = true

		val minecart = e.player.world.spawnEntity(clickedBlock.location.add(0.0, 1.0, 0.0), EntityType.MINECART)

		cars[minecart.uniqueId] = storedInfo

		e.player.sendMessage("§aOlha o seu carrão! vroom vroom")

		e.player.playSound(e.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f)

		item.amount -= 1
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	fun onClick(e: com.Acrobot.ChestShop.Events.ItemParseEvent) {
		val cleanItemString = org.bukkit.ChatColor.stripColor(e.itemString)!!


		if (cleanItemString == "Fusca") {
			val itemStack = ItemStack(Material.MINECART)
				.rename("§3§lFusca")
				.storeMetadata(FUSCA_CHECK_KEY, "true")

			e.item = itemStack
		}
	}
}