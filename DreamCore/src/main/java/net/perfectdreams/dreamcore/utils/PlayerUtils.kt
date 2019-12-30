package net.perfectdreams.dreamcore.utils

import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object PlayerUtils {
	/**
	 * Deixa o jogador com a vida máxima possível e enche a barrinha de comida do jogdaor
	 *
	 * @param player o jogador
	 */
	fun healAndFeed(player: Player) {
		player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 10.0
		player.foodLevel = 20
	}

	fun canBreakAt(loc: Location, p: Player, m: Material): Boolean {
		val claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null)
		var canBuildClaim: String? = null
		if (claim != null) {
			canBuildClaim = claim.allowBreak(p, m)
		}
		return canBuildClaim == null && WorldGuardUtils.canBreakAt(loc, p)
	}

	fun canPlaceAt(loc: Location, p: Player, m: Material): Boolean {
		val claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null)
		var canBuildClaim: String? = null
		if (claim != null) {
			canBuildClaim = claim.allowBuild(p, m)
		}
		return canBuildClaim == null && WorldGuardUtils.canBuildAt(loc, p)
	}
}