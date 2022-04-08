package net.perfectdreams.dreamxizum.battle

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.extensions.available
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.tasks.UpdateLeaderboardTask.bestDuelist
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class BattleListener : Listener {
    private val lobby = XizumConfig.models.locations.lobby.toBukkitLocation()

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        with (event) {
            if (cause == TeleportCause.CHORUS_FRUIT && !player.available) { return@with setCancelled(true) }
            if (cause == TeleportCause.ENDER_PEARL) return@with
            if (to.world == XizumConfig.xizumWorld && to.distance(lobby) > 5) return@with

            player.battle?.let {
                if (it.stage > BattleStage.CREATING_BATTLE) it.removeFromBattle(player, BattleDeathReason.TELEPORTED)
            }

            Matchmaker.removeFromQueue(player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        with (event.player) {
            battle?.let {
                if (it.stage > BattleStage.CREATING_BATTLE) it.removeFromBattle(this, BattleDeathReason.DISCONNECTED)
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        with (event.player) {
            battle?.let {
                if (it.stage > BattleStage.CREATING_BATTLE) it.removeFromBattle(this, BattleDeathReason.KILLED)
            }
        }
    }

    @EventHandler
    fun onDropItem(event: PlayerDropItemEvent) { with (event) { player.battle?.let { isCancelled = true } } }
    @EventHandler
    fun onDamageItem(event: PlayerItemDamageEvent) { with (event) { player.battle?.let { isCancelled = it.options.itemsType == BattleItems.PLAYER_ITEMS } } }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onDamage(event: EntityDamageByEntityEvent) {
        with (event) {
            if (damager is Player)
                with (damager as Player) {
                    battle?.let { isCancelled = !it.canHit(this, entity as Player) }
                }
        }
    }

    @EventHandler
    fun onApplyTag(event: ApplyPlayerTagsEvent) {
        with (event) {
            if (player.uniqueId == bestDuelist)
                tags.add(
                    PlayerTag(
                        "§x§0§e§d§6§f§b§lD",
                        "§x§0§e§d§6§f§b§lD§x§0§e§c§f§f§7§lu§x§0§d§c§7§f§2§le§x§0§d§c§0§e§e§ll§x§0§d§b§9§e§9§li§x§0§d§b§2§e§5§ls§x§0§c§a§a§e§0§lt§x§0§c§a§3§d§c§la",
                        listOf(
                            "§r§b${player.name}§r§7 atualmente ocupa a primeira",
                            "colocação no modo competitivo do Xizum."
                        ),
                        "/xizum",
                        false,
                    )
                )
        }
    }
}