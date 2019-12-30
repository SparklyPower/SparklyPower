package net.perfectdreams.dreamcore.utils.extensions

import com.comphenix.protocol.wrappers.nbt.NbtCompound
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.tags.NbtTagsUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.getCompoundTag() = NbtTagsUtils.getCompoundTag(this)
fun ItemStack.setCompoundTag(tag: NbtCompound) = NbtTagsUtils.setCompoundTag(this, tag)
fun ItemStack.getStoredMetadata(key: String) = ItemUtils.getStoredMetadata(this, key)
fun ItemStack.storeMetadata(key: String, value: String) = ItemUtils.storeMetadata(this, key, value)
fun ItemStack.hasStoredMetadataWithKey(key: String) = ItemUtils.hasStoredMetadataWithKey(this, key)
fun ItemStack.getTranslatedDisplayName(player: Player) =  ItemUtils.getTranslatedDisplayName(this, player)
fun ItemStack.getTranslatedDisplayName(locale: String) =  ItemUtils.getTranslatedDisplayName(this, locale)
fun Material.toItemStack(amount: Int = 1) = ItemStack(this, amount)
inline fun <reified T> ItemStack.meta(block: T.() -> Unit): ItemStack {
	val itemMeta = (this.itemMeta as T).apply {
		block.invoke(this)
	}
	this.itemMeta = itemMeta as ItemMeta
	return this
}