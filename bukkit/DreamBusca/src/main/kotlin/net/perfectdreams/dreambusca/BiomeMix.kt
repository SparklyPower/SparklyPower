package net.perfectdreams.dreambusca

import org.bukkit.block.Biome

enum class BiomeMix {
	ALL_MESAS,
	ALL_DESERTS,
	ALL_JUNGLES,
	ALL_SWAMPLANDS,
	ALL_BIRCH,
	ALL_OCEANS,
	ALL_TAIGAS,
	ALL_MEGATAIGAS,
	ALL_EXTREME,
	ALL_PLAINS,
	ALL_ICEPLAINS,
	ALL_FORESTS,
	ALL_SAVANNAS,
	ALL_BIOMES,
	ALL_ROOFED,
	ALL_MUSHROOM;

	fun isBiomeOfKind(b: Biome): Boolean {
		when {
			this == ALL_MESAS && b.toString().contains("BADLANDS") -> return true
			this == ALL_DESERTS && b.toString().contains("DESERT") -> return true
			this == ALL_JUNGLES && b.toString().contains("JUNGLE") -> return true
			this == ALL_SWAMPLANDS && b.toString().contains("SWAMP") -> return true
			this == ALL_BIRCH && b.toString().contains("BIRCH") -> return true
			this == ALL_TAIGAS && b.toString().contains("TAIGA") && !b.toString().contains("SPRUCE") -> return true
			this == ALL_MEGATAIGAS && b.toString().contains("TAIGA") && b.toString().contains("GIANT") && b.toString().contains("SPRUCE") -> return true
			this == ALL_EXTREME && b.toString().contains("MOUNTAIN") -> return true
			this == ALL_PLAINS && b.toString().contains("PLAINS") && !b.toString().contains("ICE") -> return true
			this == ALL_ICEPLAINS && b == Biome.ICE_SPIKES -> return true
			this == ALL_FORESTS && b.toString().contains("FOREST") && !b.toString().contains("BADLANDS") && !b.toString().contains("BIRCH") && !b.toString().contains("DARK_FOREST") -> return true
			this == ALL_SAVANNAS && b.toString().contains("SAVANNA") -> return true
			this == ALL_ROOFED && b.toString().contains("DARK_FOREST") -> return true
			this == ALL_OCEANS && b.toString().contains("OCEAN") -> return true
			this == ALL_MUSHROOM && b.toString().contains("MUSHROOM") -> return true
			else -> return this == ALL_BIOMES
		}
	}

}