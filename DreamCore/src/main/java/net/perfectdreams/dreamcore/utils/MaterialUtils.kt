package net.perfectdreams.dreamcore.utils

import org.bukkit.Material
import org.bukkit.Tag



object MaterialUtils {
	// O jogador consegue ficar dentro destes materiais
	val HOLLOW_MATERIALS = mutableSetOf<Material>()
	val TRANSPARENT_MATERIALS = mutableSetOf<Material>()

	init {
		// Materiais de Material.isTransparent()
		HOLLOW_MATERIALS.add(Material.AIR)
		HOLLOW_MATERIALS.addAll(Tag.SAPLINGS.values)
		HOLLOW_MATERIALS.add(Material.POWERED_RAIL)
		HOLLOW_MATERIALS.add(Material.DETECTOR_RAIL)
		HOLLOW_MATERIALS.add(Material.TALL_GRASS)
		HOLLOW_MATERIALS.add(Material.DEAD_BUSH)
		HOLLOW_MATERIALS.add(Material.BROWN_MUSHROOM)
		HOLLOW_MATERIALS.add(Material.RED_MUSHROOM)
		HOLLOW_MATERIALS.add(Material.TORCH)
		HOLLOW_MATERIALS.add(Material.FIRE)
		HOLLOW_MATERIALS.add(Material.REDSTONE_WIRE)
		HOLLOW_MATERIALS.add(Material.WHEAT)
		HOLLOW_MATERIALS.add(Material.LADDER)
		HOLLOW_MATERIALS.add(Material.RAIL)
		HOLLOW_MATERIALS.add(Material.LEVER)
		HOLLOW_MATERIALS.add(Material.REDSTONE_TORCH)
		HOLLOW_MATERIALS.add(Material.STONE_BUTTON)
		HOLLOW_MATERIALS.add(Material.SNOW)
		HOLLOW_MATERIALS.add(Material.SUGAR_CANE)
		HOLLOW_MATERIALS.add(Material.NETHER_PORTAL)
		HOLLOW_MATERIALS.add(Material.REPEATER)
		HOLLOW_MATERIALS.add(Material.PUMPKIN_STEM)
		HOLLOW_MATERIALS.add(Material.MELON_STEM)
		HOLLOW_MATERIALS.add(Material.VINE)
		HOLLOW_MATERIALS.add(Material.LILY_PAD)
		HOLLOW_MATERIALS.add(Material.NETHER_WART)
		HOLLOW_MATERIALS.add(Material.END_PORTAL)
		HOLLOW_MATERIALS.add(Material.COCOA)
		HOLLOW_MATERIALS.add(Material.TRIPWIRE_HOOK)
		HOLLOW_MATERIALS.add(Material.TRIPWIRE)
		HOLLOW_MATERIALS.add(Material.FLOWER_POT)
		HOLLOW_MATERIALS.add(Material.CARROT)
		HOLLOW_MATERIALS.add(Material.POTATO)
		HOLLOW_MATERIALS.addAll(Tag.BUTTONS.values)
		// HOLLOW_MATERIALS.add(Material.SKULL)
		HOLLOW_MATERIALS.add(Material.COMPARATOR)
		HOLLOW_MATERIALS.add(Material.ACTIVATOR_RAIL)
		HOLLOW_MATERIALS.addAll(Tag.CARPETS.values)
		HOLLOW_MATERIALS.add(Material.SUNFLOWER)

		// Outros materiais adicionados pelo Essentials
		// HOLLOW_MATERIALS.add(Material.SEEDS)
		HOLLOW_MATERIALS.add(Material.ACACIA_SIGN)
		HOLLOW_MATERIALS.add(Material.BIRCH_SIGN)
		HOLLOW_MATERIALS.add(Material.DARK_OAK_SIGN)
		HOLLOW_MATERIALS.add(Material.JUNGLE_SIGN)
		HOLLOW_MATERIALS.add(Material.OAK_SIGN)
		HOLLOW_MATERIALS.add(Material.SPRUCE_SIGN)
		HOLLOW_MATERIALS.addAll(Tag.DOORS.values)
		HOLLOW_MATERIALS.add(Material.ACACIA_WALL_SIGN)
		HOLLOW_MATERIALS.add(Material.BIRCH_WALL_SIGN)
		HOLLOW_MATERIALS.add(Material.DARK_OAK_WALL_SIGN)
		HOLLOW_MATERIALS.add(Material.JUNGLE_WALL_SIGN)
		HOLLOW_MATERIALS.add(Material.OAK_WALL_SIGN)
		HOLLOW_MATERIALS.add(Material.SPRUCE_WALL_SIGN)
		HOLLOW_MATERIALS.addAll(Tag.WOODEN_PRESSURE_PLATES.values)
		HOLLOW_MATERIALS.add(Material.ACACIA_FENCE_GATE)
		HOLLOW_MATERIALS.add(Material.BIRCH_FENCE_GATE)
		HOLLOW_MATERIALS.add(Material.DARK_OAK_FENCE_GATE)
		HOLLOW_MATERIALS.add(Material.JUNGLE_FENCE_GATE)
		HOLLOW_MATERIALS.add(Material.OAK_FENCE_GATE)
		HOLLOW_MATERIALS.add(Material.SPRUCE_FENCE_GATE)

		TRANSPARENT_MATERIALS.addAll(HOLLOW_MATERIALS)
		TRANSPARENT_MATERIALS.add(Material.WATER)
	}

	fun getTranslationKey(material: Material): String {
		val namespacedKey = material.key
		val root = if (material.isBlock) "block" else "item"
		return root + "." + namespacedKey.namespace + "." + namespacedKey.key
	}
}