package net.perfectdreams.dreamxizum.tasks

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.BattleType
import net.perfectdreams.dreamxizum.dao.Duelist
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import java.util.UUID
import kotlin.math.abs

object RankedQueueTask {
    val battleOptions = BattleOptions().apply {
        timeLimit = 5
        items = mutableListOf(Material.DIAMOND_SWORD.toItemStack())
        armor = setOf(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS)
            .mapTo(mutableSetOf()) { it.toItemStack().apply { addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4) } }
    }

    fun startTask() =
        DreamXizum.INSTANCE.schedule {
            while(true) {
                with (Matchmaker.rankedQueue) {
                    val matched = mutableSetOf<RankedQueueUser>()
                    sortBy { it.startTime }

                    forEach { user ->
                        if (user in matched) return@forEach

                        val suitableMatches = filter { candidate ->
                            (candidate.duelist.points in (user.duelist.points - 395 .. user.duelist.points + 395)) &&
                            (candidate.duelist.uuid != user.duelist.uuid) &&
                            (candidate !in matched)
                        }

                        suitableMatches.minByOrNull { abs(user.duelist.points - it.duelist.points) }?.let { paired ->
                            val battle = Matchmaker.createBattle(BattleType.RANKED, 2).apply {
                                options = battleOptions
                            }

                            with(setOf(user, paired)) {
                                val players = map { it.duelist.uuid.toPlayer() }

                                players.forEach { player ->
                                    battle.addToBattle(player)
                                    val enemy = players.first { player.uniqueId != it.uniqueId }
                                    val enemyPoints = first { player.uniqueId != it.duelist.uuid }.duelist.points
                                    player.sendMessage("${DreamXizum.PREFIX} Você caiu contra ${highlight("${enemy.name} (${enemyPoints} pontos)")}.")
                                }

                                matched.addAll(this)
                            }
                        }
                    }
                    removeAll(matched)
                }

                waitFor(100L)
            }
        }

    private fun UUID.toPlayer() = DreamXizum.INSTANCE.server.getPlayer(this)!!
}

class RankedQueueUser(val duelist: Duelist, var startTime: Long = System.currentTimeMillis())