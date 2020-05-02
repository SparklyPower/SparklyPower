package net.perfectdreams.dreamterrainadditions

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamterrainadditions.commands.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.io.File

class DreamTerrainAdditions : KotlinPlugin(), Listener {
	var claimsAdditionsList = mutableListOf<ClaimAdditions>()

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)

		registerCommand(BanirCommand)
		registerCommand(DesbanirCommand)
		registerCommand(RetirarCommand)
		registerCommand(ConfigureClaimCommand)

		dataFolder.mkdir()

		if (File(dataFolder, "additions.json").exists()) {
			claimsAdditionsList = DreamUtils.gson.fromJson(File(dataFolder, "additions.json").readText())
		}
	}

	fun save() {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			File(dataFolder, "additions.json").writeText(DreamUtils.gson.toJson(claimsAdditionsList))
		}
	}

	@EventHandler
	fun onWalk(e: PlayerMoveEvent) {
		if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ)
			return

		val claim = GriefPrevention.instance.dataStore.getClaimAt(e.to, false, null) ?: return
		val claimAdditions = getClaimAdditionsById(claim.id) ?: return

		if (claimAdditions.bannedPlayers.contains(e.player.name)) {
			e.isCancelled = true

			e.player.sendTitle("", "§cVocê está banido deste terreno", 0, 60, 0)
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun getClaimAdditionsById(claimId: Long): ClaimAdditions? {
		return claimsAdditionsList.firstOrNull { it.claimId == claimId }
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
		EntityType.PIG_ZOMBIE,
		EntityType.EVOKER,
		EntityType.ILLUSIONER,
		EntityType.GHAST,
		EntityType.HUSK,
		EntityType.STRAY
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

	@EventHandler
	fun onDamage(e: EntityDamageByEntityEvent) {
		val damager = e.damager
		val entity = e.entity

		if (damager is Player && entity is Player) {
			val entityClaim = GriefPrevention.instance.dataStore.getClaimAt(e.entity.location, false, null) ?: return
			val damagerClaim = GriefPrevention.instance.dataStore.getClaimAt(e.damager.location, false, null) ?: return

			if (entityClaim == damagerClaim) {
				val claimAdditions = getClaimAdditionsById(entityClaim.id)
				val pvpEnabled = claimAdditions?.pvpEnabled ?: false

				e.isCancelled = !pvpEnabled
			}
		}
	}

	class ClaimAdditions(val claimId: Long) {
		val bannedPlayers = mutableListOf<String>()
		var pvpEnabled = false
		var disablePassiveMobs = false
		var disableHostileMobs = false
	}
}