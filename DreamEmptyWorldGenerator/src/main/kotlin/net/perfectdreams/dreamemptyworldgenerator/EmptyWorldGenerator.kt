package net.perfectdreams.dreamemptyworldgenerator

import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import java.util.*

class EmptyWorldGenerator : ChunkGenerator() {
	override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
		return createChunkData(world)
	}
}