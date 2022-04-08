package net.perfectdreams.dreamxizum.battle

import net.perfectdreams.dreamcore.utils.WrapperHologram
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object BattleHolograms {
    private val holograms = mutablePlayerMapOf<WrapperHologram>()

    private const val POSITIVE = "§x§f§f§c§0§0§0§l"
    private const val NEGATIVE = "§x§f§3§8§c§0§1§l"

    fun createAuthorHologram(battle: Battle) {
        val author = battle.author!!

        val lines = mutableListOf("§x§a§9§b§4§0§b§l⤖ §nJogadores§x§a§9§b§4§0§b§l ⬻").apply {
            for (index in 1 .. battle.limit)
                if (index == 1) add("${POSITIVE}1. ${author.name}")
                else add("$NEGATIVE$index.")
        }

        val vector = offset(battle.limit/2, 0)
        WrapperHologram(author.location.add(vector), lines).apply {
            holograms[author] = this
            addViewer(author)
        }
    }

    fun createHologram(battle: Battle, player: Player) {
        val vector = offset(battle.limit / 2, battle.indexOf(player))
        WrapperHologram(player.location.add(vector), holograms[battle.author]!!.lines).apply {
            holograms[player] = this
            addViewer(player)
        }
    }

    fun updateHolograms(battle: Battle) {
        val mainHologram = holograms[battle.author] ?: return
        val updatedLines = mainHologram.lines.apply {
            for (index in 2 .. battle.limit) {
                val color = battle[index - 1]?.let { if (it.status == BattleUserStatus.ALIVE) POSITIVE else NEGATIVE } ?: NEGATIVE
                val text = "$index. " + (battle[index - 1]?.let { it.player.name + if (it.status == BattleUserStatus.PENDING) " (pendente)" else "" } ?: "")
                set(index, color + text)
            }
        }

        battle.players.forEach { if (it?.status == BattleUserStatus.ALIVE) holograms[it.player]?.lines = updatedLines }
    }

    fun removeHologram(player: Player) =
        holograms[player]?.apply {
            removeViewer(player)
            holograms.remove(player)
        }

    fun deleteHolograms(battle: Battle) =
        battle.players.forEach {
            if (it?.status == BattleUserStatus.ALIVE) removeHologram(it.player)
        }

    private fun offset(arena: Int, index: Int) = when (arena) {
        3 -> {
            if (index < 3) Vector(1.7F, -.4F, 0F)
            else Vector(-1.7F, -.4F, 0F)
        }

        2 -> {
            if (index < 2) Vector(1.35F, -.1F, 1.35F)
            else Vector(-1.35F, -.1F, -1.35F)
        }

        1 -> {
            if (index == 0) Vector(1F, -.5F, 1F)
            else Vector(-1F, -.5F, -1F)
        }

        else -> Vector(0F, 0F, 0F)
    }
}