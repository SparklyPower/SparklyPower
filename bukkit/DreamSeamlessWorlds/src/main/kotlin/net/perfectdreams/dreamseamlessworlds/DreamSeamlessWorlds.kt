package net.perfectdreams.dreamseamlessworlds

import com.charleskorn.kaml.Yaml
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import kotlinx.coroutines.future.await
import kotlinx.serialization.decodeFromString
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.packetevents.ClientboundPacketSendEvent
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.WorldBorder
import org.bukkit.craftbukkit.CraftChunk
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftLocation
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.math.absoluteValue

class DreamSeamlessWorlds : KotlinPlugin(), Listener {
	private val seamlessAtlas = mutableMapOf<String, SeamlessWorld>()

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

		val config = Yaml.default.decodeFromString<DreamSeamlessWorldsConfig>(this.getConfig().saveToString())
		config.seamlessWorlds.forEach {
			seamlessAtlas[it.key] = SeamlessWorld(
				Bukkit.getWorld(it.key)!!,
				it.value.northWorld?.let { Bukkit.getWorld(it) },
				it.value.southWorld?.let { Bukkit.getWorld(it) },
				it.value.eastWorld?.let { Bukkit.getWorld(it) },
				it.value.westWorld?.let { Bukkit.getWorld(it) },
			)
		}

		/* seamlessAtlas["another_test_flats"] = SeamlessWorld(
			Bukkit.getWorld("another_test_flats")!!,
			null,
			Bukkit.getWorld("another_test_flats3")!!,
			Bukkit.getWorld("another_test_flats2"),
			null
		)

		seamlessAtlas["another_test_flats2"] = SeamlessWorld(
			Bukkit.getWorld("another_test_flats2")!!,
			null,
			null,
			null,
			Bukkit.getWorld("another_test_flats")!!,
		)

		seamlessAtlas["another_test_flats3"] = SeamlessWorld(
			Bukkit.getWorld("another_test_flats3")!!,
			Bukkit.getWorld("another_test_flats")!!,
			null,
			null,
			null
		) */
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (!event.displaced)
			return

		val playerWorld = event.player.world
		val seamlessWorldInfo = seamlessAtlas[playerWorld.name] ?: return // whatever then...

		val worldBorder = playerWorld.worldBorder
		val borderCenterX = worldBorder.center.x
		val borderCenterZ = worldBorder.center.z
		val borderSize = worldBorder.size / 2

		// Now we need to check which world are we bordering
		// To do this, we need to check which coordinates fall into our bordering worlds
		val minX: Double = borderCenterX - borderSize
		val maxX: Double = borderCenterX + borderSize
		val minZ: Double = borderCenterZ - borderSize
		val maxZ: Double = borderCenterZ + borderSize

		val northWorld = seamlessWorldInfo.northWorld
		val southWorld = seamlessWorldInfo.southWorld
		val eastWorld = seamlessWorldInfo.eastWorld
		val westWorld = seamlessWorldInfo.westWorld

		if (westWorld != null && minX + 0.40 >= event.to.x && event.to.z in minZ..maxZ) {
			// event.player.sendMessage("teleporting WEST")
			val target = Location(
				westWorld,
				// This should be reoffsetted
				(event.player.x.absoluteValue).coerceAtLeast(maxX - 0.50),
				event.player.y,
				event.player.z,
				event.player.yaw,
				event.player.pitch
			)
			event.player.teleport(target)
			return
		}

		if (eastWorld != null && event.to.x >= maxX - 0.40 && event.to.z in minZ..maxZ) {
			val target = Location(
				eastWorld,
				// This should be reoffsetted
				(-event.player.x.absoluteValue).coerceAtLeast(minX + 0.50),
				event.player.y,
				event.player.z,
				event.player.yaw,
				event.player.pitch
			)
			event.player.teleport(target)
			return
		}

		if (southWorld != null && event.to.z > maxZ - 0.40 && event.to.x in minX..maxX) {
			// event.player.sendMessage("teleporting SOUTH")
			val target = Location(
				southWorld,
				// This should be reoffsetted
				event.player.x,
				event.player.y,
				(-event.player.z.absoluteValue).coerceAtLeast(minZ + 0.50),
				event.player.yaw,
				event.player.pitch
			)
			event.player.teleport(target)
			return
		}

		if (northWorld != null && minZ + 0.40 > event.to.z && event.to.x in minX..maxX) {
			// event.player.sendMessage("teleporting NORTH")
			val target = Location(
				northWorld,
				// This should be reoffsetted
				event.player.x,
				event.player.y,
				event.player.z.absoluteValue.coerceAtMost(maxZ - 0.50),
				event.player.yaw,
				event.player.pitch
			)
			event.player.teleport(target)
			// event.player.sendMessage("new location: " + event.player.location)
			return
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onTickEnd(event: ServerTickEndEvent) {
		// Sync weather and time between worlds
		val mainWorld = Bukkit.getWorld("world")!!

		for ((_, connectedWorld) in seamlessAtlas) {
			if (connectedWorld.world == mainWorld)
				continue

			connectedWorld.world.fullTime = mainWorld.fullTime
			// Disable weather cycle, this will be controlled by the main world + keeping it enabled causes flood in chat because the world wants to disable weather
			// while the main world resets it
			if (connectedWorld.world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE) == true)
				connectedWorld.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
			if (connectedWorld.world.hasStorm() != mainWorld.hasStorm())
				connectedWorld.world.setStorm(mainWorld.hasStorm())
			if (connectedWorld.world.weatherDuration != mainWorld.weatherDuration)
				connectedWorld.world.weatherDuration = mainWorld.weatherDuration
			if (connectedWorld.world.isThundering != mainWorld.isThundering)
				connectedWorld.world.isThundering = mainWorld.isThundering
			if (connectedWorld.world.thunderDuration != mainWorld.thunderDuration)
				connectedWorld.world.thunderDuration = mainWorld.thunderDuration
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPacketSend(event: ClientboundPacketSendEvent) {
		// This TECHNICALLY could have some race conditions related to packets
		val msg = event.packet

		// Avoid infinite loop xd
		if (event.identifier == "dreamseamlessworld-chunk-packet")
			return

		if (msg is ClientboundLevelChunkWithLightPacket) {
			val playerWorld = event.player.world

			val seamlessWorldInfo = seamlessAtlas[playerWorld.name] ?: return // whatever then...

			// Are we outside of the world border?
			// the Y doesn't really matter
			val worldBorder = playerWorld.worldBorder
			val isInsideBorder = isChunkInsideWorldBorder(worldBorder, msg.x, msg.z)

			if (!isInsideBorder) {
				// Not inside border!
				val nmsWorld = (playerWorld as CraftWorld).handle
				val borderCenterX = worldBorder.center.x
				val borderCenterZ = worldBorder.center.z
				val borderSize = worldBorder.size / 2
				val borderSideAsChunks = (worldBorder.size / 16).toInt()

				// Now we need to check which world are we bordering
				// To do this, we need to check which coordinates fall into our bordering worlds
				val minX: Double = borderCenterX - borderSize
				val maxX: Double = borderCenterX + borderSize
				val minZ: Double = borderCenterZ - borderSize
				val maxZ: Double = borderCenterZ + borderSize
				val chunkXAsBlock = msg.x * 16
				val chunkZAsBlock = msg.z * 16

				val minXAsChunk = (minX / 16).toInt()
				val maxXAsChunk = (maxX / 16).toInt()
				val minZAsChunk = (minZ / 16).toInt()
				val maxZAsChunk = (maxZ / 16).toInt()

				// TODO: We really need to rethink this, maybe think it like if it were pixels?
				// Detecting the edge is a bit hard, but not impossible
				val targetEdge = when {
					msg.z in minZAsChunk until maxZAsChunk && minXAsChunk >= msg.x -> WorldEdgeDirection.WEST
					msg.z in minZAsChunk until maxZAsChunk && msg.x >= maxXAsChunk -> WorldEdgeDirection.EAST
					msg.x in minXAsChunk until maxXAsChunk && minXAsChunk >= msg.z -> WorldEdgeDirection.NORTH
					msg.x in minXAsChunk until maxXAsChunk && msg.z >= maxXAsChunk -> WorldEdgeDirection.SOUTH
					else -> null
				}

				val targetWorld = when (targetEdge) {
					WorldEdgeDirection.NORTH -> seamlessWorldInfo.northWorld
					WorldEdgeDirection.SOUTH -> seamlessWorldInfo.southWorld
					WorldEdgeDirection.EAST -> seamlessWorldInfo.eastWorld
					WorldEdgeDirection.WEST -> seamlessWorldInfo.westWorld
					null -> null
				}

				// println("minXAsChunk: $minXAsChunk; maxXAsChunk: $maxXAsChunk; minZAsChunk: $minZAsChunk; maxZAsChunk: $maxZAsChunk; minX: $minX; maxX: $maxX; minZ: $minZ; maxZ: $maxZ; CHUNK ${msg.x}, ${msg.z}; $chunkXAsBlock; $chunkZAsBlock TARGET WORLD IS $targetWorld - $targetEdge")

				if (targetWorld != null && targetEdge != null) {
					val nmsTargetWorld = (targetWorld as CraftWorld).handle

					// This kinda requires an "reoffset"
					// How to reoffset?
					// can't we just like... do a % operation considering that all worldborders have the same size?

					val targetChunkX = when (targetEdge) {
						WorldEdgeDirection.EAST -> -borderSideAsChunks
						WorldEdgeDirection.WEST -> borderSideAsChunks
						WorldEdgeDirection.NORTH -> 0
						WorldEdgeDirection.SOUTH -> 0
					}

					val targetChunkZ = when (targetEdge) {
						WorldEdgeDirection.EAST -> 0
						WorldEdgeDirection.WEST -> 0
						WorldEdgeDirection.NORTH -> borderSideAsChunks
						WorldEdgeDirection.SOUTH -> -borderSideAsChunks
					}

					val chunk = targetWorld.getChunkAtAsync(
						msg.x + targetChunkX,
						msg.z + targetChunkZ,
						false
					)

					launchAsyncThread {
						// delay(4_000) // artificial delay
						val loadedChunk = chunk.await()
						// println("Loaded chunk result: $loadedChunk")
						if (loadedChunk != null) {
							val nmsLoadedChunk = (loadedChunk as CraftChunk).getHandle(ChunkStatus.FULL)
							val p = ClientboundLevelChunkWithLightPacket(
								nmsLoadedChunk as LevelChunk, // concerning
								nmsTargetWorld.lightEngine,
								null,
								null,
								true
							)
							// TODO: Change SparklyPaper to let plugins set the xz of the packet
							val x = p::class.java.getDeclaredField("x")
							val z = p::class.java.getDeclaredField("z")
							x.isAccessible = true
							z.isAccessible = true
							x.set(p, msg.x)
							z.set(p, msg.z)

							// TODO: This is wrong, I'm not sure why the client isn't crashing (probably ViaVersion intercepting and removing the unknown blocks, because joining with 1.20.6 kicks the client)
							//  We need to make a "send packet without triggering THIS listener" or something like that later
							event.sendPacketWithIdentifier("dreamseamlessworld-chunk-packet", p)
							return@launchAsyncThread
						}
					}
				} else {
					// Send empty chunk instead
					val plains: Holder<Biome> = nmsWorld.registryAccess().registryOrThrow<Biome>(Registries.BIOME)
						.getHolderOrThrow(Biomes.PLAINS)

					event.packet = ClientboundLevelChunkWithLightPacket(
						net.minecraft.world.level.chunk.EmptyLevelChunk(
							nmsWorld,
							ChunkPos(msg.x, msg.z),
							plains
						),
						nmsWorld.lightEngine, null, null, true
					)
				}
				return
			}
		}
	}

	fun isChunkInsideWorldBorder(worldBorder: WorldBorder, chunkX: Int, chunkZ: Int): Boolean {
		val borderCenterX = worldBorder.center.x
		val borderCenterZ = worldBorder.center.z
		val borderSize = worldBorder.size / 2

		// Now we need to check which world are we bordering
		// To do this, we need to check which coordinates fall into our bordering worlds
		val minX: Double = borderCenterX - borderSize
		val maxX: Double = borderCenterX + borderSize
		val minZ: Double = borderCenterZ - borderSize
		val maxZ: Double = borderCenterZ + borderSize

		val minXAsChunk = (minX / 16).toInt()
		val maxXAsChunk = (maxX / 16).toInt()
		val minZAsChunk = (minZ / 16).toInt()
		val maxZAsChunk = (maxZ / 16).toInt()

		return chunkX in minXAsChunk until maxXAsChunk && chunkZ in minZAsChunk until maxZAsChunk
	}

	enum class WorldEdgeDirection {
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
}
