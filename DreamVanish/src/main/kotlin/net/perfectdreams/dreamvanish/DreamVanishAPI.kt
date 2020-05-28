package net.perfectdreams.dreamvanish

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object DreamVanishAPI {
    val vanishedPlayers = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())
    val queroTrabalharPlayers = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())

    fun isVanished(player: Player) = vanishedPlayers.contains(player)

    fun isQueroTrabalhar(player: Player) = queroTrabalharPlayers.contains(player)

    fun setVanishedStatus(player: Player, enabled: Boolean) {
        if (enabled) {
            Bukkit.getOnlinePlayers().forEach {
                if (!it.hasPermission("dreamvanish.bypassvanish")) {
                    // Can't bypass the vanish status, so...
                    it.hidePlayer(DreamVanish.INSTANCE, player)
                }
            }

            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 20 * 86400, 0, true, false))

            vanishedPlayers.add(player)
        } else {
            Bukkit.getOnlinePlayers().forEach {
                it.showPlayer(DreamVanish.INSTANCE, player)
            }

            player.removePotionEffect(PotionEffectType.INVISIBILITY)

            vanishedPlayers.remove(player)
        }
    }

    /**
     * Checks if a player is vanished or invisible
     *
     * @param player the player
     * @return if the player is vanished or invisible
     */
    fun isVanishedOrInvisible(player: Player): Boolean {
        return isVanished(player) || player.hasPotionEffect(PotionEffectType.INVISIBILITY)
    }
}