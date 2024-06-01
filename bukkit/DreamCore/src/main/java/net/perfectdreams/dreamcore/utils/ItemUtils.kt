package net.perfectdreams.dreamcore.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import protocolsupport.api.TranslationAPI
import java.io.*
import java.math.BigInteger
import java.util.*

object ItemUtils {
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

@Deprecated(message = "Please use ItemUtils.serializeItemToBase64()")
fun ItemStack.toBase64(): String {
	val outputStream = ByteArrayOutputStream();
	val dataOutput = DataOutputStream(outputStream)
	val nbtTagListItems = ListTag()
	val nbtTagCompoundItem = CompoundTag()
	val nmsItem = CraftItemStack.asNMSCopy(this)
	// TODO - 1.20.6: Fix this!
	// nmsItem.save(nbtTagCompoundItem)
	// nbtTagListItems.add(nbtTagCompoundItem)
	// NbtIo.write(nbtTagCompoundItem, dataOutput as DataOutput)
	// return BigInteger(1, outputStream.toByteArray()).toString(32);
	error("Unsupported")
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

	// TODO - 1.20.6: Fix this!
	// val nmsItem = net.minecraft.world.item.ItemStack.of(nbtTagCompoundRoot ?: error("NBT Tag Compound Root is null, why?"))
	// return CraftItemStack.asBukkitCopy(nmsItem)
	error("Unsupported")
}