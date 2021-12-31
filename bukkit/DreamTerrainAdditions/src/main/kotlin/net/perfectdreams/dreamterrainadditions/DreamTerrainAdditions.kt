package net.perfectdreams.dreamterrainadditions

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamterrainadditions.commands.*
import net.perfectdreams.dreamterrainadditions.commands.declarations.TempTrustCommand
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
import java.io.File
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
		registerCommand(TempTrustCommand, TempTrustExecutor(this), TempTrustListExecutor(this))

		dataFolder.mkdir()

		if (File(dataFolder, "additions.json").exists()) {
			Json.decodeFromString<List<ClaimAdditionsData>>(File(dataFolder, "additions.json").readText())
				.map { ClaimAdditions(it) }
				.toMutableList()
				.forEach {
					claimsAdditionsMap[it.claimId] = it
				}
		}
		startCheckingTemporaryTrustsExpirationDate()
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

	@EventHandler
	fun onWalk(e: PlayerMoveEvent) {
		if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ)
			return

		val claim = GriefPrevention.instance.dataStore.getClaimAt(e.to, false, null) ?: return
		val claimAdditions = getClaimAdditionsById(claim.id) ?: return

		if (claimAdditions.bannedPlayers.contains(e.player.name) && !e.player.hasPermission("sparklypower.soustaff")) {
			e.isCancelled = true

			e.player.sendTitle("§f", "§cVocê está banido deste terreno", 0, 60, 0)
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
		EntityType.MUSHROOM_COW,
		EntityType.OCELOT,
		EntityType.PANDA,
		EntityType.PARROT,
		EntityType.PIG,
		EntityType.POLAR_BEAR,
		EntityType.PUFFERFISH,
		EntityType.RABBIT,
		EntityType.SALMON,
		EntityType.SHEEP,
		EntityType.SNOWMAN,
		EntityType.SQUID,
		EntityType.TRADER_LLAMA,
		EntityType.TROPICAL_FISH
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
	fun onSpawn(e: EntitySpawnEvent) {
		val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.location, false, null) ?: return

		val claimAdditions = getClaimAdditionsById(entityClaim.id) ?: return
		val disablePassiveMobs = claimAdditions.disablePassiveMobs
		val disableHostileMobs = claimAdditions.disableHostileMobs

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
		if (event.source.type == Material.BROWN_MUSHROOM|| event.block.type == Material.RED_MUSHROOM || event.block.type == Material.VINE) {
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

	@EventHandler
	fun onTrustChange(event: TrustChangedEvent) {
		for (claim in event.claims) {
			val claimAdditions = getClaimAdditionsById(claim.id) ?: return

			val userUniqueId = DreamUtils.retrieveUserUniqueId(event.identifier)
			if (claimAdditions.temporaryTrustedPlayers.containsKey(userUniqueId) && !event.isGiven) {
				claimAdditions.temporaryTrustedPlayers.remove(userUniqueId)
			}
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
		var disableTrapdoorAndDoorAccess: Boolean = false
	)

	object UUIDAsStringSerializer : KSerializer<UUID> {
		override val descriptor: SerialDescriptor =
			PrimitiveSerialDescriptor("UUIDAsStringSerializer", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UUID) {
			encoder.encodeString(value.toString())
		}

		override fun deserialize(decoder: Decoder): UUID {
			return UUID.fromString(decoder.decodeString())
		}
	}
}