package net.perfectdreams.dreamemptyworldgenerator

import org.bukkit.plugin.java.JavaPlugin

class DreamEmptyWorldGenerator : JavaPlugin() {
	private val generator = EmptyWorldGenerator()

	override fun getDefaultWorldGenerator(worldName: String, id: String?) = generator
}