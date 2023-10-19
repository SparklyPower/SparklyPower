package net.perfectdreams.dreamcore.utils.extensions

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.okkero.skedule.schedule
import me.ryanhamshire.GriefPrevention.Claim
import me.ryanhamshire.GriefPrevention.ClaimPermission
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerSetOf
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

fun Player.canBreakAt(location: Location, material: Material) = PlayerUtils.canBreakAt(location, this, material)
fun Player.canPlaceAt(location: Location, material: Material) = PlayerUtils.canPlaceAt(location, this, material)
fun Player.healAndFeed() = PlayerUtils.healAndFeed(this)

var Player.girl: Boolean
    get() = MeninaAPI.isGirl(this.uniqueId)
    set(value) {
        MeninaAPI.setGirlStatus(this.uniqueId, value)
        return
    }

val Player.pronome: String
    get() = MeninaAPI.getPronome(this)

val Player.artigo: String
    get() = MeninaAPI.getArtigo(this)

val partiallyHiddenPlayers = mutableSetOf<UUID>().also { set ->
    ProtocolLibrary.getProtocolManager().addPacketListener(
        object : PacketAdapter(DreamCore.INSTANCE, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_INFO) {
            override fun onPacketSending(event: PacketEvent) {
                with (event.packet) {
                    if (playerInfoAction.read(0) != EnumWrappers.PlayerInfoAction.REMOVE_PLAYER) return@with
                    if (playerInfoDataLists.read(0)[0].profile.uuid !in set) return@with
                    event.isCancelled = true
                }
            }
        }
    )

    DreamCore.INSTANCE.registerEvents(
        object : Listener {
            @EventHandler
            fun onJoin(event: PlayerJoinEvent) {
                DreamCore.INSTANCE.schedule {
                    waitFor(20L)
                    set.forEach { DreamCore.INSTANCE.server.getPlayer(it)!!.hidePartially() }
                }
            }

            @EventHandler
            fun onQuit(event: PlayerQuitEvent) = event.player.showPartiallyHiddenPlayer()
        }
    )
}

/**
 * Partially hides [Player]. They will still become invisible,
 * but their name will remain in the tab list.
 */
fun Player.hidePartially() {
    partiallyHiddenPlayers.add(this.uniqueId)
    DreamCore.INSTANCE.server.onlinePlayers.forEach {
        it.hidePlayer(DreamCore.INSTANCE, this)
    }
}

/**
 * Shows [Player] who was partially hidden.
 */
fun Player.showPartiallyHiddenPlayer() {
    partiallyHiddenPlayers.remove(this.uniqueId)
    DreamCore.INSTANCE.server.onlinePlayers.forEach {
        it.showPlayer(DreamCore.INSTANCE, this)
    }
}

val frozenPlayers = mutablePlayerSetOf { it.unfreeze() }.also { set ->
    DreamCore.INSTANCE.registerEvents(
        object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun onProjectileLaunch(event: ProjectileLaunchEvent) = with(event.entity.shooter) {
                if (this is Player && this in set) event.isCancelled = true
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun onInteraction(event: PlayerInteractEvent) {
                if (event.player in set) event.isCancelled = true
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun onMove(event: PlayerMoveEvent) {
                if (event.player in set)
                    event.isCancelled = compareValuesBy(event.from, event.to, { it.x }, { it.y }, { it.z }) != 0
            }
        }
    )
}

/**
 * Freezes [Player] and prevents them from interacting with the server.
 */
fun Player.freeze() {
    addPotionEffect(PotionEffect(PotionEffectType.JUMP, 99999, 128, false, false))
    frozenPlayers.add(this)
    walkSpeed = 0F
}

/**
 * Unfreezes [Player] and allows them to interact with the server.
 */
fun Player.unfreeze() {
    removePotionEffect(PotionEffectType.JUMP)
    frozenPlayers.remove(this)
    walkSpeed = .2F
}

val EMPTY_ITEM = Material.AIR.toItemStack()
val playerInventories = mutablePlayerMapOf<Array<ItemStack>> { player, content ->
    player.inventory.contents = content
}

/**
 * Stores [Player]'s inventory.
 */
fun Player.storeInventory() {
    playerInventories[this] = inventory.contents.map { it ?: EMPTY_ITEM }.toTypedArray()
    inventory.clear()
}

/**
 * Retrieves [Player]'s stored inventory.
 */
fun Player.restoreInventory() = playerInventories[this]?.let { inventory.contents = it  }

/**
 * Plays [sound] and sends [message] to [Player].
 */
fun Player.playSoundAndSendMessage(sound: Sound, message: String) {
    playSound(location, sound, 10F, 1F)
    sendMessage(message)
}

/**
 * @return Whether [Player] has [permission] at [claim] or not.
 */
private fun Player.hasPermissionAtClaim(permission: ClaimPermission, claim: Claim, staffBypass: Boolean) =
    claim.hasExplicitPermission(this, permission) || (staffBypass && this.isStaff)

/**
 * @return Whether the owner of the claim has trusted [Player] to build at said claim.
 */
fun Player.canBuildAtClaim(claim: Claim, staffBypass: Boolean) =
    this.hasPermissionAtClaim(ClaimPermission.Build, claim, staffBypass)

/**
 * @return Whether the owner of the claim has trusted [Player] to manage said claim.
 */
fun Player.canManageClaim(claim: Claim, staffBypass: Boolean) =
    this.hasPermissionAtClaim(ClaimPermission.Manage, claim, staffBypass)

/**
 * Since we are only interested in whether [Player] has either [ClaimPermission.Build] or [ClaimPermission.Manage]
 * permissions, there is no need to check for the other types.
 */
fun Player.hasAnyPermissionAtClaim(claim: Claim, staffBypass: Boolean) =
    this.canBuildAtClaim(claim, staffBypass) || this.canManageClaim(claim, staffBypass)