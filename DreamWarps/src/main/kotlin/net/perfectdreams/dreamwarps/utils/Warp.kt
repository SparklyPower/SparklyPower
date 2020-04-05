package net.perfectdreams.dreamwarps.utils

import org.bukkit.Location
import org.bukkit.Material

class Warp(var name: String, var location: Location) {
	var priority: Int = 0
	var fancyName: String = name
	var description: List<String> = mutableListOf<String>()
	var material: Material = Material.DIAMOND
	var icon: String = "https://i.imgur.com/VgHMyU7.png"
	var display = false
}