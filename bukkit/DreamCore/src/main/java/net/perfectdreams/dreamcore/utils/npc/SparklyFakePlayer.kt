package net.perfectdreams.dreamcore.utils.npc

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

// This is unused, maybe in the future we should modify SparklyPaper to create entities with this exact ticking code
class SparklyFakePlayer(world: Level) : net.minecraft.world.entity.monster.Husk(
    EntityType.HUSK,
    world
) {
    override fun tick() {
        // Always reset the invulnerable time, allows players to kill the NPC
        this.invulnerableTime = 0

        // We only tick if we are dead or dying, this is from LivingEntity's baseTick code
        // This is needed because, if not, the entity is never removed from the world after being killed
        if (this.isDeadOrDying && level().shouldTickDeath(this)) {
            tickDeath()
        }
    }
}