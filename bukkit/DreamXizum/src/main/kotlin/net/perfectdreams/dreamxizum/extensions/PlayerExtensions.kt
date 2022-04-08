package net.perfectdreams.dreamxizum.extensions

import net.luckperms.api.node.types.PermissionNode
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerSetOf
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamxizum.DreamXizum.Companion.legacyAttackSpeed
import net.perfectdreams.dreamxizum.DreamXizum.Companion.luckPerms
import net.perfectdreams.dreamxizum.battle.Battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.kits.PlayerKitsPage
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

val playerBattles = mutablePlayerMapOf<Battle>()
val unavailablePlayers = mutablePlayerSetOf()

fun Player.prepareToBattle() {
    removeAllPotionEffects()
    freeze()
    clearTitle()
    healAndFeed()
    hidePartially()
    storeInventory()
    available = false
    saturation = 1000F
    isGlowing = false
}

fun Player.freeFromBattle() {
    unfreeze()
    saturation = 20F
    available = true
    legacyPvp = true
    allowMcMMO = true
    resetPlayerTime()
    allowAllCommands()
    restoreInventory()
    showPartiallyHiddenPlayer()
    Paginator.fetch(this).destroy()
    PlayerKitsPage.namingKit.remove(this)
}

val permissionNode = PermissionNode.builder("mcmmo.*").value(false).build()

var Player.allowMcMMO: Boolean
    get() = TODO()
    set(value) {
        luckPerms.userManager.modifyUser(uniqueId) {
            with (it.data()) { if (value) remove(permissionNode) else add(permissionNode) }
        }
    }

var Player.legacyPvp: Boolean
    get() = with(getAttribute(Attribute.GENERIC_ATTACK_SPEED)) {
        this!!.baseValue != 4.0
    }
    set(value) = with(getAttribute(Attribute.GENERIC_ATTACK_SPEED)) {
        this!!.baseValue = if (value) legacyAttackSpeed else 4.0
    }

var Player.available
    get() = this !in unavailablePlayers
    set(value) = unavailablePlayers.let { if (value) it.remove(this) else it.add(this) }

var Player.battle: Battle?
    get() = playerBattles[this]
    set(value) { value?.let { playerBattles[this] = it } ?: playerBattles.remove(this) }