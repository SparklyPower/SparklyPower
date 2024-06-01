package net.perfectdreams.dreamterrainadditions

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import me.ryanhamshire.GriefPrevention.events.ClaimChangeEvent
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent
import me.ryanhamshire.GriefPrevention.events.ClaimResizeEvent
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.VaultUtils
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.serializer.UUIDAsStringSerializer
import net.perfectdreams.dreamterrainadditions.commands.*
import net.perfectdreams.dreamterrainadditions.commands.declarations.TempTrustCommand
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class DreamTerrainAdditions : KotlinPlugin(), Listener {
	// We use a Map to make all claims additions check O1
	// Using a list would require looping all claim additions just to check what matches what we want...
	// and that's SUPER intensive!
	//
	// TODO: Maybe also save the data as a Map instead of a List!
	var claimsAdditionsMap = mutableMapOf<Long, ClaimAdditions>()

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)

		registerCommand(BanirCommand)
		registerCommand(DesbanirCommand)
		registerCommand(RetirarCommand)
		registerCommand(ConfigureClaimCommand)
		registerCommand(ListarBanidosCommand)
		registerCommand(TempTrustCommand(this))
		registerCommand(DreamTerrainAdditionsCommand(this))

		dataFolder.mkdir()

		if (File(dataFolder, "additions.json").exists()) {
			Json.decodeFromString<List<ClaimAdditionsData>>(File(dataFolder, "additions.json").readText())
				.map { ClaimAdditions(it) }
				.forEach {
					claimsAdditionsMap[it.claimId] = it
				}
		}
		startCheckingTemporaryTrustsExpirationDate()

		/* launchAsyncThread {
			// task tasky task
			// get all claims
			// this is HARD
			// we need to know how many players
			val playerToClaimSizes = mutableMapOf<UUID, Long>()

			val claimList = onAsyncThread { GriefPrevention.instance.dataStore.claims.toList() } // We are in an async task, let's create a copy of the original list
			for (claim in claimList) {
				playerToClaimSizes[claim.ownerID] = playerToClaimSizes.getOrDefault(claim.ownerID, 0) + claim.area
			}

			// Now we get the money, and restrict claims that didn't pay the price
			for ((playerId, claimSize) in playerToClaimSizes) {
				val offlinePlayer = Bukkit.getOfflinePlayer(playerId)
				val valueToBeRemoved = claimSize.toDouble()

				if (VaultUtils.econ.has(offlinePlayer, valueToBeRemoved)) {
					VaultUtils.econ.withdrawPlayer(offlinePlayer, valueToBeRemoved)
				} else {
					// Restrict the current player's claim
				}
			}

			// Done! (yay)
		} */
	}

	override fun softDisable() {
		super.softDisable()
		save()
	}

	fun saveInAsyncTask() {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			save()
		}
	}

	private fun save() {
		File(dataFolder, "additions.json").writeText(
			Json.encodeToString(
				claimsAdditionsMap.values
					.map { it.data }
			)
		)
	}

	/* @EventHandler
	fun onClaim(e: ClaimCreatedEvent) {
		val creator = e.creator
		if (creator is Player && creator.world.name == "Survival2") {
			val userClaims = GriefPrevention.instance.dataStore.getPlayerData(e.claim.ownerID).claims
			val totalClaimedArea = userClaims.filter { it.greaterBoundaryCorner.world.name == "Survival2" }.sumOf { it.area } + e.claim.area

			if (totalClaimedArea >= 10000) {
				e.isCancelled = true
				creator.sendMessage("§cAtualmente não é possível proteger mais de 10000 blocos no survival2!")
			}
		}
	}

	@EventHandler
	fun onResize(e: ClaimResizeEvent) {
		val creator = e.modifier
		if (creator is Player && creator.world.name == "Survival2") {
			val userClaims = GriefPrevention.instance.dataStore.getPlayerData(e.claim.ownerID).claims
			val totalClaimedArea = userClaims.filter { it.greaterBoundaryCorner.world.name == "Survival2" }.sumOf { it.area } + e.claim.area

			if (totalClaimedArea >= 10000) {
				e.isCancelled = true
				creator.sendMessage("§cAtualmente não é possível proteger mais de 10000 blocos no survival2!")
			}
		}
	} */

	@EventHandler
	fun onWalk(e: PlayerMoveEvent) {
		if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ)
			return

		val claim = GriefPrevention.instance.dataStore.getClaimAt(e.to, false, null) ?: return
		val claimAdditions = getClaimAdditionsById(claim.id) ?: return

		// If we are staff, we can bypass the ban
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (claimAdditions.bannedPlayers.contains(e.player.name)) {
			e.isCancelled = true

			e.player.sendTitle("§f", "§cVocê está banido deste terreno", 0, 60, 0)
		} else if (claimAdditions.blockAllPlayersExceptTrusted && !(claim.ownerID == e.player.uniqueId || claim.hasExplicitPermission(e.player, ClaimPermission.Build))) {
			e.isCancelled = true

			e.player.sendTitle("§f", "§cO dono não deixa outros players entrarem no terreno!", 0, 60, 0)
		}
	}

	@EventHandler
	fun onTeleport(e: PlayerTeleportEvent) {
		val claim = GriefPrevention.instance.dataStore.getClaimAt(e.to, false, null) ?: return
		val claimAdditions = getClaimAdditionsById(claim.id) ?: return

		// If we are staff, we can bypass the ban
		if (e.player.hasPermission("sparklypower.soustaff"))
			return

		if (claimAdditions.bannedPlayers.contains(e.player.name)) {
			e.isCancelled = true

			e.player.sendTitle("§f", "§cVocê está banido deste terreno", 0, 60, 0)
		} else if (claimAdditions.blockAllPlayersExceptTrusted && !(claim.ownerName == e.player.name || claim.hasExplicitPermission(e.player, ClaimPermission.Build))) {
			e.isCancelled = true

			e.player.sendTitle("§f", "§cO dono não deixa outros players entrarem no terreno!", 0, 60, 0)
		}
	}

	fun getClaimAdditionsById(claimId: Long): ClaimAdditions? {
		return claimsAdditionsMap[claimId]
	}

	fun getOrCreateClaimAdditionsWithId(claimId: Long): ClaimAdditions {
		return getClaimAdditionsById(claimId) ?: return ClaimAdditions(ClaimAdditionsData(claimId)).also { claimsAdditionsMap[claimId] = it }
	}

	val passiveMobs = listOf(
		EntityType.BAT,
		EntityType.BEE,
		EntityType.CAT,
		EntityType.CHICKEN,
		EntityType.COD,
		EntityType.COW,
		EntityType.DOLPHIN,
		EntityType.DONKEY,
		EntityType.FOX,
		EntityType.HORSE,
		EntityType.IRON_GOLEM,
		EntityType.LLAMA,
		EntityType.MULE,
		EntityType.MOOSHROOM,
		EntityType.OCELOT,
		EntityType.PANDA,
		EntityType.PARROT,
		EntityType.PIG,
		EntityType.POLAR_BEAR,
		EntityType.PUFFERFISH,
		EntityType.RABBIT,
		EntityType.SALMON,
		EntityType.SHEEP,
		EntityType.SNOW_GOLEM,
		EntityType.SQUID,
		EntityType.TRADER_LLAMA,
		EntityType.TROPICAL_FISH,
		EntityType.FROG,

	)

	val aggressiveMobs = listOf(
		EntityType.CREEPER,
		EntityType.ZOMBIE,
		EntityType.SKELETON,
		EntityType.SPIDER,
		EntityType.CAVE_SPIDER,
		EntityType.DROWNED,
		EntityType.ELDER_GUARDIAN,
		EntityType.GUARDIAN,
		EntityType.ENDERMAN,
		EntityType.BLAZE,
		EntityType.GHAST,
		EntityType.WITCH,
		EntityType.ZOMBIE_VILLAGER,
		EntityType.ZOMBIE_HORSE,
		EntityType.PIGLIN,
		EntityType.ZOMBIFIED_PIGLIN,
		EntityType.EVOKER,
		EntityType.ILLUSIONER,
		EntityType.GHAST,
		EntityType.HUSK,
		EntityType.STRAY,
		EntityType.SLIME,
		EntityType.MAGMA_CUBE
	)

	@EventHandler
	fun onTrustChanged(e: TrustChangedEvent) {
		launchMainThread {
			// If it is all, we will remove ALL permissions
			if (e.identifier == "all") {
				for (claim in e.claims) {
					val claimAdditions = getClaimAdditionsById(claim.id) ?: return@launchMainThread

					if (e.claimPermission == null) {
						// Claim permission was removed!
						claimAdditions.temporaryTrustedPlayersMutex.withLock {
							claimAdditions.temporaryTrustedPlayers.clear()
						}

						saveInAsyncTask()
					}
				}
				return@launchMainThread
			}

			val affectedId = try {
				UUID.fromString(e.identifier)
			} catch (e: IllegalArgumentException) {
				// The identifier can be other values other than a UUID, such as "all", "public", etc
				return@launchMainThread
			}

			for (claim in e.claims) {
				val claimAdditions = getClaimAdditionsById(claim.id) ?: return@launchMainThread

				if (e.claimPermission == null) {
					// Claim permission was removed!
					claimAdditions.temporaryTrustedPlayersMutex.withLock {
						claimAdditions.temporaryTrustedPlayers.remove(affectedId)
					}

					saveInAsyncTask()
				}
			}
		}
	}

	@EventHandler
	fun onSpawn(e: EntitySpawnEvent) {
		val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.location, false, null) ?: return

		val claimAdditions = getClaimAdditionsById(entityClaim.id) ?: return
		val disablePassiveMobs = claimAdditions.disablePassiveMobs
		val disableHostileMobs = claimAdditions.disableHostileMobs

		if (e.entity.fromMobSpawner() && claimAdditions.allowSpawnFromMobSpawners)
			return

		if (disablePassiveMobs && e.entityType in passiveMobs)
			e.isCancelled = true

		if (disableHostileMobs && e.entityType in aggressiveMobs)
			e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onDamage(e: EntityDamageByEntityEvent) {
		val damager = e.damager
		val entity = e.entity

		val realDamager = if (damager is Projectile) {
			if (damager.shooter is Player) {
				damager.shooter as Player
			} else damager
		} else damager

		if (realDamager is Player && entity is Player) {
			val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.entity.location, false, null) ?: return
			val damagerClaim = GriefPrevention.instance.dataStore.getClaimAt(realDamager.location, false, null) ?: return

			if (entityClaim == damagerClaim) {
				val claimAdditions = getClaimAdditionsById(entityClaim.id)
				val pvpEnabled = claimAdditions?.pvpEnabled ?: false

				e.isCancelled = !pvpEnabled
			}
		}
	}

	@EventHandler
	fun onBlockGrowth(e: BlockGrowEvent) {
		val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null) ?: return
		val cropsGrowthDisabled = getClaimAdditionsById(entityClaim.id)?.disableCropGrowth ?: return
		if (cropsGrowthDisabled) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onBlockSpread(event: BlockSpreadEvent) {
		if (event.source.type == Material.BROWN_MUSHROOM || event.source.type == Material.RED_MUSHROOM || event.source.type == Material.VINE) {
			val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(event.block.location, false, null) ?: return
			val plantsSpreadingDisabled = getClaimAdditionsById(entityClaim.id)?.disablePlantsSpreading ?: return
			if (plantsSpreadingDisabled) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onBlockForm(e: BlockFormEvent) {
		val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null) ?: return

		val claimAdditions = getClaimAdditionsById(entityClaim.id) ?: return
		val disableSnowFormation = claimAdditions.disableSnowFormation

		val type = e.newState.type

		if (disableSnowFormation && type == Material.SNOW)
			e.isCancelled = true
	}

	@EventHandler
	fun onPlayerInteractEvent(e: PlayerInteractEvent) {
		val clickedBlock = e.clickedBlock ?: return

		val checkItem = clickedBlock.type.name.toUpperCase().contains("(_TRAPDOOR|_DOOR)".toRegex())

		if (checkItem) {

			val claim = GriefPrevention.instance.dataStore.getClaimAt(clickedBlock.location, false, null) ?: return

			if (claim.ownerName == e.player.name)
				return

			if (claim.hasExplicitPermission(e.player, ClaimPermission.Build))
				return

			if (claim.allowGrantPermission(e.player) == null)
				return

			val claimAdditions = getClaimAdditionsById(claim.id) ?: return

			val disableTrapdoorAndDoorAccess = claimAdditions.disableTrapdoorAndDoorAccess

			if (disableTrapdoorAndDoorAccess)
				e.isCancelled = true

		}
	}

	private fun startCheckingTemporaryTrustsExpirationDate() {
		ClaimTrustExpirationTask(this).runTaskTimerAsynchronously(this, 20L, 120L);
	}

	class ClaimAdditions(
		val data: ClaimAdditionsData
	) {
		val claimId by data::claimId
		val bannedPlayers by data::bannedPlayers
		val temporaryTrustedPlayers by data::temporaryTrustedPlayers
		var pvpEnabled by data::pvpEnabled
		var disableCropGrowth by data::disableCropGrowth
		var disablePassiveMobs by data::disablePassiveMobs
		var disableHostileMobs by data::disableHostileMobs
		var disableSnowFormation by data::disableSnowFormation
		var disablePlantsSpreading by data::disablePlantsSpreading
		var disableTrapdoorAndDoorAccess by data::disableTrapdoorAndDoorAccess
		var allowSpawnFromMobSpawners by data::allowSpawnFromMobSpawners
		var blockAllPlayersExceptTrusted by data::blockAllPlayersExceptTrusted

		val temporaryTrustedPlayersMutex = Mutex()
	}

	@Serializable
	data class ClaimAdditionsData(
		val claimId: Long,
		val bannedPlayers: MutableList<String> = mutableListOf(),
		val temporaryTrustedPlayers: MutableMap<@Serializable(UUIDAsStringSerializer::class) UUID, Long> = mutableMapOf(),
		var pvpEnabled: Boolean = false,
		var disableCropGrowth: Boolean = false,
		var disablePassiveMobs: Boolean = false,
		var disableHostileMobs: Boolean = false,
		var disableSnowFormation: Boolean = false,
		var disablePlantsSpreading: Boolean = false,
		var disableTrapdoorAndDoorAccess: Boolean = false,
		var allowSpawnFromMobSpawners: Boolean = false,
		var blockAllPlayersExceptTrusted: Boolean = false
	)
}