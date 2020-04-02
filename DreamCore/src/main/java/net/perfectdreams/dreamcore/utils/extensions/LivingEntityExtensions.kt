package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

fun LivingEntity.removeAllPotionEffects() = this.activePotionEffects.forEach { this.removePotionEffect(it.type) }