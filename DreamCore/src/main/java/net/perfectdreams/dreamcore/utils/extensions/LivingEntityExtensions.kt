package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.entity.LivingEntity

fun LivingEntity.removeAllPotionEffects() = this.activePotionEffects.forEach { this.removePotionEffect(it.type) }