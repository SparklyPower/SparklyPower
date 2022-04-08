package net.perfectdreams.dreamxizum.battle.npcs

import net.perfectdreams.dreamcore.utils.createNPC
import net.perfectdreams.dreamxizum.battle.Battle
import net.perfectdreams.dreamxizum.battle.BattleType
import net.perfectdreams.dreamxizum.battle.BattleUserStatus
import net.perfectdreams.dreamxizum.config.XizumConfig
import org.bukkit.Particle

class Referee(val battle: Battle) {
    private val model = XizumConfig.models.npcs.referee
    private val location = with (XizumConfig.models.locations.arenas) {
        if (battle.type == BattleType.RANKED) ranked.referee else when (battle.limit / 2) {
            3 -> x3.referee
            2 -> x2.referee
            else -> x1.referee
        }
    }.toBukkitLocation()

    private val npc = createNPC(model.displayName, location) {
        skin {
            texture = model.skin.texture
            signature = model.skin.signature
        }
    }.apply { shouldSpawnPunching = true }

    fun spawn() = battle.players.forEach {
        if (it?.status != BattleUserStatus.ALIVE) return@forEach
        it.player.spawnParticle(Particle.CLOUD, location, 100, -.25, 1.0, -.25)
        npc.addViewer(it.player)
    }

    fun destroy() = npc.destroy()
    fun punch() = npc.punch()
}