package net.perfectdreams.dreamcore.utils.tags

import com.comphenix.protocol.wrappers.nbt.NbtCompound
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object NbtTagsUtils {
	const val SERVER_DATA_COMPOUND_NAME = "PerfectDreams"

	fun getCompoundTag(item: ItemStack): NbtCompound? {
		return NbtFactory.asCompound(
				NbtFactory.fromItemTag(
						CraftItemStack.asCraftCopy(item)
				)
		)
	}

	fun setCompoundTag(item: ItemStack, tag: NbtCompound): ItemStack {
		val clonedItem = CraftItemStack.asCraftCopy(item)
		NbtFactory.setItemTag(clonedItem, tag)
		return clonedItem
	}
}