package net.perfectdreams.dreamcore.utils

import com.meowj.langutils.lang.LanguageHelper
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun ItemStack.getLocalizedDisplayName(locale: String = "pt_br"): String {
	return LanguageHelper.getItemDisplayName(this, locale)
}

fun ItemStack.getLocalizedDisplayName(player: Player): String {
	return LanguageHelper.getItemDisplayName(this, player)
}

fun ItemStack.getLocalizedName(locale: String = "pt_br"): String {
	return LanguageHelper.getItemName(this, locale)
}

fun ItemStack.getLocalizedName(player: Player): String {
	return LanguageHelper.getItemName(this, player)
}

fun Enchantment.getLocalizedName(locale: String = "pt_br"): String {
	return LanguageHelper.getEnchantmentName(this, locale)
}

fun Enchantment.getLocalizedName(player: Player): String {
	return LanguageHelper.getEnchantmentName(this, player)
}

fun Entity.getLocalizedDisplayName(locale: String = "pt_br"): String {
	return LanguageHelper.getEntityDisplayName(this, locale)
}

fun Entity.getLocalizedDisplayName(player: Player): String {
	return LanguageHelper.getEntityDisplayName(this, player)
}

fun Entity.getLocalizedName(locale: String = "pt_br"): String {
	return LanguageHelper.getEntityName(this, locale)
}

fun Entity.getLocalizedName(player: Player): String {
	return LanguageHelper.getEntityName(this, player)
}

fun EntityType.getLocalizedName(locale: String = "pt_br"): String {
	return LanguageHelper.getEntityName(this, locale)
}

fun EntityType.getLocalizedName(player: Player): String {
	return LanguageHelper.getEntityName(this, player)
}