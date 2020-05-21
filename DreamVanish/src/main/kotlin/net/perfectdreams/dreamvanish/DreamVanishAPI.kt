package net.perfectdreams.dreamvanish

import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer
import org.bukkit.entity.Player
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

            vanishedPlayers.add(player)
        } else {
            Bukkit.getOnlinePlayers().forEach {
                it.showPlayer(DreamVanish.INSTANCE, player)
            }

            vanishedPlayers.remove(player)
        }
    }

    fun isVanishedOrInvisible(player: Player): Boolean {
        return isVanished(player) || player.hasPotionEffect(PotionEffectType.INVISIBILITY)
    }
}