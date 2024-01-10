package net.perfectdreams.dreamcore.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.perfectdreams.dreamcore.utils.extensions.getCompoundTag
import net.perfectdreams.dreamcore.utils.extensions.setCompoundTag
import net.perfectdreams.dreamcore.utils.tags.NbtTagsUtils
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import protocolsupport.api.TranslationAPI
import java.io.*
import java.math.BigInteger
import java.util.*

object ItemUtils {
	/**
	 * Returns the value of the [key] metadata in the [itemStack], if it doesn't exists, returns null
	 */
	fun getStoredMetadata(itemStack: ItemStack, key: String): String? {
		try {
			val tag = itemStack.getCompoundTag() ?: return null
			if (!hasStoredMetadataWithKeyAtNbtRoot(itemStack, NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)) return null
			val compound = tag.getCompound(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)
			if (!compound.containsKey(key))
				return null
			return compound.getString(key)
		} catch (e: IllegalArgumentException) {
			// ItemStacks representing air cannot store NMS information. & outros erros do ProtocolLib
			return null
		}
	}

	/**
	 * Stores the specific [value] on the [key] metadata in the [itemStack], returning a copy of the original [itemStack] with the modified NBT Tag
	 */
	fun storeMetadata(itemStack: ItemStack, key: String, value: String): ItemStack {
		val tag = itemStack.getCompoundTag() ?: return itemStack
		val compound = tag.getCompoundOrDefault(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME)
		compound.put(key, value)

		if (!tag.containsKey(NbtTagsUtils.SERVER_DATA_COMPOUND_NAME))
			tag.put(compound)

		return itemStack.setCompoundTag(tag)
	}

	/**
	 * Checks if the [itemStack] has the [key] metadata in it's NBT Root Tag
	 */
	private fun hasStoredMetadataWithKeyAtNbtRoot(itemStack: ItemStack, key: String): Boolean {
		val tag = itemStack.getCompoundTag() ?: return false
		return tag.containsKey(key)
	}

	/**
	 * Checks if the [itemStack] has the [key] metadata in it's NBT Tag
	 */
	fun hasStoredMetadataWithKey(itemStack: ItemStack, key: String) = getStoredMetadata(itemStack, key) != null

	fun getTranslatedDisplayName(itemStack: ItemStack, player: Player): String {
		return getTranslatedDisplayName(itemStack, player.locale)
	}

	fun getTranslatedDisplayName(itemStack: ItemStack, locale: String): String {
		if (itemStack.itemMeta.hasDisplayName())
			return itemStack.itemMeta.displayName
		return TranslationAPI.getTranslationString(locale, MaterialUtils.getTranslationKey(itemStack.type))!!
	}

	fun serializeItemToBase64(itemStack: ItemStack): String {
		// This is better and the deserialized item does go through data fixer upper
		val byteArray = itemStack.serializeAsBytes()
		return Base64.getEncoder().encodeToString(byteArray)
	}

	fun deserializeItemFromBase64(base64Item: String): ItemStack {
		// This is better and the deserialized item does go through data fixer upper
		return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64Item))
	}
}

fun ItemStack.rename(name: String): ItemStack {
	val meta = this.itemMeta
	meta.setDisplayName(name)
	this.itemMeta = meta
	return this
}

fun ItemStack.lore(vararg lore: String): ItemStack {
	val meta = this.itemMeta
	meta.lore = Arrays.asList(*lore)
	this.itemMeta = meta
	return this
}

fun ItemStack.lore(lore: List<String>): ItemStack {
	val meta = this.itemMeta
	meta.lore = lore
	this.itemMeta = meta
	return this
}

fun ItemStack.addFlag(vararg flag: ItemFlag): ItemStack {
	val meta = this.itemMeta
	meta.addItemFlags(*flag)
	this.setItemMeta(meta)
	return this
}

fun ItemStack.removeFlag(vararg flag: ItemFlag): ItemStack {
	val meta = this.itemMeta
	meta.removeItemFlags(*flag)
	this.itemMeta = meta
	return this
}

@Deprecated(message = "Please use ItemExtensions.setCompoundTag(...)")
fun ItemStack.setStorageData(data: String, key: UUID): ItemStack {
	return ItemUtils.storeMetadata(this, key.toString(), data)
}

@Deprecated(message = "Please use ItemExtensions.getCompoundTag()")
fun ItemStack.getStorageData(key: UUID): String? {
	return ItemUtils.getStoredMetadata(this, key.toString())
}

@Deprecated(message = "Please use ItemUtils.serializeItemToBase64()")
fun ItemStack.toBase64(): String {
	val outputStream = ByteArrayOutputStream();
	val dataOutput = DataOutputStream(outputStream)
	val nbtTagListItems = ListTag()
	val nbtTagCompoundItem = CompoundTag()
	val nmsItem = CraftItemStack.asNMSCopy(this)
	nmsItem.save(nbtTagCompoundItem)
	nbtTagListItems.add(nbtTagCompoundItem)
	NbtIo.write(nbtTagCompoundItem, dataOutput as DataOutput)
	return BigInteger(1, outputStream.toByteArray()).toString(32);
}

@Deprecated(message = "Please use ItemUtils.deserializeItemFromBase64()")
fun String.fromBase64Item(): ItemStack {
	val inputStream = ByteArrayInputStream(BigInteger(this, 32).toByteArray())
	var nbtTagCompoundRoot: CompoundTag? = null
	try {
		nbtTagCompoundRoot = NbtIo.read(DataInputStream(inputStream) as DataInput)
	} catch (e: IOException) {
		e.printStackTrace()
	}

	val nmsItem = net.minecraft.world.item.ItemStack.of(nbtTagCompoundRoot ?: error("NBT Tag Compound Root is null, why?"))
	return CraftItemStack.asBukkitCopy(nmsItem)
}