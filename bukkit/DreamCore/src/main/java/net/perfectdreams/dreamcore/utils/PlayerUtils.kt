package net.perfectdreams.dreamcore.utils

import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

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
		// Performance: https://github.com/TechFortress/GriefPrevention/issues/1438#issuecomment-872363793
		var canBuildClaim = true

		if (claim != null) // The supplier can be "null"!
			canBuildClaim = claim.checkPermission(p, ClaimPermission.Build, CompatBuildBreakEvent(m, true)) == null

		return canBuildClaim && WorldGuardUtils.canBreakAt(loc, p)
	}

	fun canPlaceAt(loc: Location, p: Player, m: Material): Boolean {
		val claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null)
		// Performance: https://github.com/TechFortress/GriefPrevention/issues/1438#issuecomment-872363793
		var canBuildClaim = true

		if (claim != null) // The supplier can be "null"!
			canBuildClaim = claim.checkPermission(p, ClaimPermission.Build, CompatBuildBreakEvent(m, false)) == null

		return canBuildClaim && WorldGuardUtils.canBuildAt(loc, p)
	}

	// From GriefPrevention
	class CompatBuildBreakEvent(val material: Material, val isBreak: Boolean) : Event() {
		override fun getHandlers(): HandlerList {
			return HandlerList()
		}
	}
}