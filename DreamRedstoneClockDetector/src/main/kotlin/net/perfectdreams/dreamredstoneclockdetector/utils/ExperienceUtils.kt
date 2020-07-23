package net.perfectdreams.dreamvipstuff.utils

import org.bukkit.entity.Player

object ExperienceUtils {
    // Calculate amount of EXP needed to level up
    fun getExpToLevelUp(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    // Calculate total experience up to a level
    fun getExpAtLevel(level: Int): Int {
        return if (level <= 16) {
            (Math.pow(level.toDouble(), 2.0) + 6 * level).toInt()
        } else if (level <= 31) {
            (2.5 * Math.pow(level.toDouble(), 2.0) - 40.5 * level + 360.0).toInt()
        } else {
            (4.5 * Math.pow(level.toDouble(), 2.0) - 162.5 * level + 2220.0).toInt()
        }
    }

    // Calculate player's current EXP amount
    fun getPlayerExp(player: Player): Int {
        var exp = 0
        val level: Int = player.getLevel()

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level)

        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp())
        return exp
    }

    // Give or take EXP
    fun changePlayerExp(player: Player, exp: Int): Int {
        // Get player's current exp
        val currentExp = getPlayerExp(player)

        // Reset player's current exp to 0
        player.exp = 0f
        player.level = 0

        // Give the player their exp back, with the difference
        val newExp = currentExp + exp
        player.giveExp(newExp)

        // Return the player's new exp amount
        return newExp
    }
}