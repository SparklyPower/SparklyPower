package net.perfectdreams.dreambedrockbypass

import fr.neatmonster.nocheatplus.checks.CheckType
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo
import fr.neatmonster.nocheatplus.hooks.AbstractNCPHook
import org.bukkit.entity.Player

class Hook(val m: DreamBedrockBypass) : AbstractNCPHook() {
    override fun getHookVersion() = "1.0.0"
    override fun getHookName() = "BedrockBypass"

    // If the player is logged in via Geyser, then automatically cancel all check failures.
    override fun onCheckFailure(p0: CheckType, p1: Player, p2: IViolationInfo) = m.geyserUsers.contains(p1.uniqueId)
}