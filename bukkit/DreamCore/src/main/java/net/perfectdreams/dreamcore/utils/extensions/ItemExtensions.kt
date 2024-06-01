package net.perfectdreams.dreamcore.utils.extensions

import net.perfectdreams.dreamcore.utils.ItemUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.getTranslatedDisplayName(player: Player) =  ItemUtils.getTranslatedDisplayName(this, player)
fun ItemStack.getTranslatedDisplayName(locale: String) =  ItemUtils.getTranslatedDisplayName(this, locale)
fun ItemStack.removeAllEnchantments() = enchantments.keys.forEach { removeEnchantment(it) }
fun ItemStack.hasEnchantments() = enchantments.isNotEmpty()
fun Material.toItemStack(amount: Int = 1) = ItemStack(this, amount)
inline fun <reified T> ItemStack.meta(block: T.() -> Unit): ItemStack {
	val itemMeta = (this.itemMeta as T).apply {
		block.invoke(this)
	}
	this.itemMeta = itemMeta as ItemMeta
	return this
}