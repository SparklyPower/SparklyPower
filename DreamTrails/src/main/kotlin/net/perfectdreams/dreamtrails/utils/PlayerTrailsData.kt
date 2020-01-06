package net.perfectdreams.dreamtrails.utils

import org.bukkit.Particle

class PlayerTrailsData {
    var activeParticles = mutableListOf<Particle>()
    var activeHalo: Halo? = null

    @Transient
    var cooldowns = HashMap<Particle, Long>()
}