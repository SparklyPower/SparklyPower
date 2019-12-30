package net.perfectdreams.dreamcore.utils

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.flags.Flags
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

object WorldGuardUtils {
	fun isWithinRegion(block: Block, region: String): Boolean {
		return isWithinRegion(block.location, region)
	}

	fun isWithinRegion(player: Player, region: String): Boolean {
		return isWithinRegion(player.location, region)
	}

	fun isWithinRegion(loc: Location, region: String): Boolean {
		val regionContainer = WorldGuard.getInstance().platform.regionContainer
		val regionManager = regionContainer[BukkitAdapter.adapt(loc.world)] ?: return false
		val set = regionManager.getApplicableRegions(BukkitAdapter.adapt(loc).toVector().toBlockPoint())
		return set.any { it.id.equals(region, ignoreCase = true) }
	}

	/**
	 * Retorna todas as regiões do WorldGuard em uma localização específica
	 *
	 * @return um set com todas as regiões na localização
	 */
	fun getRegionsAt(loc: Location): ApplicableRegionSet {
		val regionContainer = WorldGuard.getInstance().platform.regionContainer
		val regionManager = regionContainer[BukkitAdapter.adapt(loc.world)]!!
		return regionManager.getApplicableRegions(BukkitAdapter.adapt(loc).toVector().toBlockPoint())
	}

	/**
	 * Retorna ID de todas as regiões do WorldGuard em uma localização específica
	 *
	 * @return um set com todas as regiões na localização
	 * @see getRegionsAt
	 */
	fun getRegionIdsAt(loc: Location): List<String> {
		return getRegionsAt(loc).map { it.id }
	}

	fun canBuildAt(l: Location, p: Player): Boolean {
		val query = WorldGuard.getInstance().platform.regionContainer.createQuery()
		val loc = BukkitAdapter.adapt(l)
		return if (!hasBypass(p, l)) {
			query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD)
		} else {
			true
		}
	}

	fun canBreakAt(l: Location, p: Player): Boolean  = canBuildAt(l, p)

	fun hasBypass(p: Player, l: Location): Boolean {
		return WorldGuard.getInstance().platform.sessionManager.hasBypass(WorldGuardPlugin.inst().wrapPlayer(p), BukkitWorld(l.world))
	}
}
