package net.perfectdreams.dreamcore.utils.extensions

import com.sk89q.worldguard.protection.ApplicableRegionSet
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.WorldGuardUtils
import org.bukkit.Location

/**
 * Retorna se a localização está dentro de uma determinada região do WorldGuard
 *
 * @param  region o ID da região
 * @return        se a localização está dentro da região
 * @see           WorldGuardUtils.isWithinRegion
 */
fun Location.isWithinRegion(region: String): Boolean {
	return WorldGuardUtils.isWithinRegion(this, region)
}

/**
 * Retorna todas as regiões que a localização está dentro dela
 *
 * @return todas as regiões em que a localização está dentro
 * @see WorldGuardUtils.getRegionsAt
 */
val Location.worldGuardRegions: ApplicableRegionSet
	get() = WorldGuardUtils.getRegionsAt(this)

fun Location.isBetween(loc1: Location, loc2: Location): Boolean = LocationUtils.isLocationBetweenLocations(this, loc1, loc2)

val Location.isAboveAir: Boolean
	get() = LocationUtils.isBlockAboveAir(this.world, this.blockX, this.blockY, this.blockZ)

val Location.isUnsafe: Boolean
	get() = LocationUtils.isBlockUnsafe(this.world, this.blockX, this.blockY, this.blockZ)

val Location.isDamaging: Boolean
	get() = LocationUtils.isBlockDamaging(this.world, this.blockX, this.blockY, this.blockZ)

val Location.rounded: Boolean
	get() = LocationUtils.isBlockDamaging(this.world, this.blockX, this.blockY, this.blockZ)

fun Location.getRoundedDestination(): Location = LocationUtils.getRoundedDestination(this)

fun Location.getSafeDestination(): Location = LocationUtils.getSafeDestination(this)