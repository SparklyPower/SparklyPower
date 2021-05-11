package net.perfectdreams.dreamlobbyfun.utils

import org.bukkit.Material

class CycleGlass {
	companion object {
		val glassTypes = listOf(
				Material.GRAY_STAINED_GLASS_PANE,
				Material.CYAN_STAINED_GLASS_PANE,
				Material.LIGHT_BLUE_STAINED_GLASS_PANE,
				Material.CYAN_STAINED_GLASS_PANE
		)
	}
	var damageValue: Int = 0

	fun next(): Material {
		if (damageValue == glassTypes.size)
			damageValue = 0
		return glassTypes[damageValue++]
	}
}