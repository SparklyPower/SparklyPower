package net.perfectdreams.dreamcustomitems.listeners

import com.okkero.skedule.schedule
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType
import com.viaversion.viaversion.api.type.types.chunk.ChunkSectionType1_18
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap
import it.unimi.dsi.fastutil.shorts.ShortSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import net.perfectdreams.dreamcustomitems.utils.CustomBlocks
import net.sparklypower.sparklypaper.event.block.PlayerBlockDestroySpeedEvent
import net.sparklypower.sparklypaper.event.packet.ClientboundPacketPreDispatchEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.absoluteValue

class CustomBlocksListener(
    val m: DreamCustomItems
) : Listener {
    companion object {
        val chunkSectionType = ChunkSectionType1_18(Block.BLOCK_STATE_REGISTRY.size(), BuiltInRegistries.BIOME_SOURCE.size())
        val TARGET_BLOCK_DEFAULT_STATE = (
                Bukkit.createBlockData(Material.TARGET) {
                    it as AnaloguePowerable
                    it.power = 0
                } as CraftBlockData
                ).state
        val TARGET_BLOCK_DEFAULT_STATE_ID = Block.BLOCK_STATE_REGISTRY.getId(TARGET_BLOCK_DEFAULT_STATE)

        private val chiseledBookshelfBlockedInteractions = listOf(
            Material.BOOK,
            Material.WRITABLE_BOOK,
            Material.WRITTEN_BOOK,
            Material.ENCHANTED_BOOK,
            Material.KNOWLEDGE_BOOK,
        )
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val hitBlock = event.hitBlock ?: return

        // Bukkit.broadcastMessage("Block hit is ${hitBlock}")

        if (hitBlock.type != Material.CHISELED_BOOKSHELF)
            return

        // We need to resync because that causes the block to change the state on the client side when hitting it with an arrow
        // Doing an update resyncs the block state on the client
        hitBlock.world.players.forEach {
            it.sendBlockChange(hitBlock.location, hitBlock.blockData)
        }

        m.launchMainThread {
            // However SOMETIMES it doesn't work, so we will update it with a delay
            delayTicks(4)
            hitBlock.world.players.forEach {
                it.sendBlockChange(hitBlock.location, hitBlock.blockData)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        // Block any book related interactions against a bookshelf
        val hitBlock = e.clickedBlock ?: return
        if (hitBlock.type != Material.CHISELED_BOOKSHELF)
            return

        val blockState = hitBlock.state as ChiseledBookshelf
        val customBlockKey = blockState.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY) ?: return
        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)

        if (customBlock != null) {
            // Block putting books altogether
            if (e.item?.type in chiseledBookshelfBlockedInteractions) {
                e.isCancelled = true
                return
            }

            // Now here's the thing: The server doesn't allow you to place blocks near chiseled bookshelves... but why?
            // Because if we are clicking on the side that there is books, the server thinks that you are trying to remove books from the bookshelf!
            // To workaround this, we will change the player's state to sneaking
            // We could also workaround this by editing the server... maybe we should do that later
            if (e.player.isSneaking)
                return

            e.player.isSneaking = true

            m.schedule {
                // Reset sneak after 1 tick
                waitFor(1L)

                if (e.player.isSneaking)
                    e.player.isSneaking = false
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onBlockDropItem(event: BlockDropItemEvent) {
        val blockState = event.blockState as? ChiseledBookshelf ?: return
        val customBlockKey = blockState.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY) ?: return
        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)

        if (customBlock != null) {
            // println("Custom block!")
            val drops = customBlock.drops.invoke()
            val dropsAsLiterallyItemDrops = drops.map {
                event.block.world.dropItem(event.block.location, it)
            }
            event.items.clear()
            event.items.addAll(dropsAsLiterallyItemDrops)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCustomBlockPlace(e: BlockPlaceEvent) {
        val customBlock = CustomBlocks.allCustomBlocks.firstOrNull { it.sourceItemCheck.invoke(e.itemInHand) }

        if (customBlock == null) {
            // Null, let's just ignore it then
            return
        }

        e.block.type = Material.CHISELED_BOOKSHELF
        val state = e.block.state as ChiseledBookshelf
        state.persistentDataContainer.set(CustomBlocks.CUSTOM_BLOCK_KEY, customBlock.id)
        state.update(true, false)
    }

    @EventHandler
    fun onBlockDamage(event: PlayerBlockDestroySpeedEvent) {
        // println("PlayerBlockDestroySpeedEvent: ${event.block.type} is ${event.destroySpeed}")

        if (event.block.type != Material.CHISELED_BOOKSHELF)
            return

        val blockState = event.block.state as? ChiseledBookshelf ?: return
        val customBlockKey = blockState.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY) ?: return
        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)

        if (customBlock != null) {
            event.destroySpeed = 0.5f
        }
    }

    @EventHandler
    fun onClientboundPacketPreDispatch(event: ClientboundPacketPreDispatchEvent) {
        // THE MEAT:TM: OF THE CUSTOM BLOCK STUFF (this is VERY hardcore)
        // Reminder that is MOSTLY (some packets may not be ran on the main thread) ran on the main thread, so this needs to be quick and snappy!
        // Keep in mind that a LOT of these requires SparklyPaper's "Helpful NMS packet changes" to avoid reflection usage
        val packet = event.packet

        val player = event.player
        val playerWorld = event.player?.world

        if (player != null && playerWorld != null) {
            val isBedrockClient = player.isBedrockClient

            if (packet is ClientboundLevelChunkWithLightPacket) {
                // println("Map chunk packet")
                // This is actually pretty weird
                // there is actually a "nested" (?) packet
                // and because ProtocolLib reads the non-nested packet, we need to cast to the NMS object and read it from there
                //
                // Very useful! https://nms.screamingsandals.org/
                val clientboundLevelChunkWithLightPacket = packet
                val chunkX = clientboundLevelChunkWithLightPacket.x
                val chunkZ = clientboundLevelChunkWithLightPacket.z
                val coordinateX = clientboundLevelChunkWithLightPacket.x * 16
                val coordinateZ = clientboundLevelChunkWithLightPacket.z * 16
                val chunkDataPacket = clientboundLevelChunkWithLightPacket.chunkData

                val worldMinHeight = playerWorld.minHeight
                val worldMaxHeight = playerWorld.maxHeight
                val worldTrueHeight = (worldMinHeight.absoluteValue + worldMaxHeight)
                val ySectionCount = worldTrueHeight / 16

                // And then we read the byte array from there!
                val readBuffer = chunkDataPacket.readBuffer
                val byteArray = readBuffer.array()

                // println("Chunk at $coordinateX, $coordinateZ")

                // println("Current Chunk: $chunkX, $chunkZ")
                val buf = Unpooled.copiedBuffer(byteArray)

                val sections = (0 until ySectionCount)
                    .map { chunkSectionType.read(buf) }

                var requiresEdits = false
                // println("World: ${event.player.world.minHeight}")
                // println("Sections: $ySectionCount")

                for ((i, section) in sections.withIndex()) {
                    val blockPalette = section.palette(PaletteType.BLOCKS) ?: continue // Does not have any block palette...

                    // Quick fail: Only edit if the palette contains chiseled bookshelfs
                    val hasCustomBlockSourceOrBlockTargetInPalette = (0 until blockPalette.size()).any {
                        val blockId = blockPalette.idByIndex(it)
                        val nmsBlockState = Block.BLOCK_STATE_REGISTRY.byId(blockId)
                        nmsBlockState?.bukkitMaterial == Material.CHISELED_BOOKSHELF || nmsBlockState?.bukkitMaterial == Material.TARGET
                    }

                    // Does not have any custom block sources in the block palette...
                    if (!hasCustomBlockSourceOrBlockTargetInPalette)
                        continue

                    for (y in 0 until 16) {
                        for (x in 0 until 16) {
                            for (z in 0 until 16) {
                                val blockId = section.getFlatBlock(x, y, z)

                                val blockData = Block.BLOCK_STATE_REGISTRY.byId(blockId)

                                if (blockData != null && blockData.`is`(Blocks.TARGET)) {
                                    // Revert it to the default state
                                    section.setFlatBlock(
                                        x,
                                        y,
                                        z,
                                        TARGET_BLOCK_DEFAULT_STATE_ID
                                    )
                                } else if (blockData != null && blockData.`is`(Blocks.CHISELED_BOOKSHELF)) {
                                    // It is a chiseled bookshelf!
                                    val blockX = coordinateX + x
                                    val blockY = y + (16 * i) - worldMinHeight.absoluteValue // The sections are from the bottom to the top, so the section 0 is at the world's min height!
                                    val blockZ = coordinateZ + z

                                    val state = playerWorld.getBlockAt(blockX, blockY, blockZ).state as ChiseledBookshelf
                                    val customBlockKey = state.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY)
                                    if (customBlockKey != null) {
                                        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)
                                        if (customBlock != null) {
                                            section.setFlatBlock(
                                                x,
                                                y,
                                                z,
                                                if (isBedrockClient) customBlock.fallbackBlockStateId else customBlock.targetBlockStateId
                                            )
                                            requiresEdits = true
                                        } else {
                                            m.logger.warning("I don't know any Sparkly Custom Block with ID \"$customBlockKey\"! Skipping...")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (requiresEdits) {
                    // println("Requires edit, so we are going to clear the read buffer")
                    // Only rewrite the packet if we really need to edit the packet
                    val byteBuf = Unpooled.buffer()
                    sections.forEach {
                        chunkSectionType.write(byteBuf, it)
                    }

                    chunkDataPacket.buffer = byteBuf.array() // This requires SparklyPaper's "Helpful NMS packet changes"
                }
                return
            }

            // ATTENTION! ClientboundBlockUpdatePacket and ClientboundSectionBlocksUpdatePacket reuse packets
            // To properly send them downstream
            if (packet is ClientboundBlockUpdatePacket) {
                // println("ClientboundBlockUpdatePacket for $player is in ${Thread.currentThread()}")
                val updatePacket = packet
                // println("ClientboundBlockUpdatePacket for $player is $packet in ${Thread.currentThread()}, isBedrockClient? $isBedrockClient")

                // println("WrapperPlayServerBlockChange for ${updatePacket.pos.x}, ${updatePacket.pos.y}, ${updatePacket.pos.z} - ${updatePacket.blockState.bukkitMaterial}")

                // If it is a target block, we need to sync it to 0 power
                if (updatePacket.blockState.bukkitMaterial == Material.TARGET) {
                    // We create a new packet here because the server resends the same packet to multiple players
                    // This is not a specific issue about us mind you, ProtocolLib has the same issue https://github.com/dmulloy2/ProtocolLib/issues/929
                    event.packet = ClientboundBlockUpdatePacket(
                        updatePacket.pos,
                        TARGET_BLOCK_DEFAULT_STATE
                    )
                } else if (updatePacket.blockState.bukkitMaterial == Material.CHISELED_BOOKSHELF) {
                    val blockInWorld = playerWorld.getBlockAt(updatePacket.pos.x, updatePacket.pos.y, updatePacket.pos.z).state as ChiseledBookshelf
                    val customBlockKey = blockInWorld.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY)

                    if (customBlockKey != null) {
                        // println("WrapperPlayServerBlockChange IS CUSTOM BLOCK!")

                        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)
                        if (customBlock != null) {
                            // Oh no, NMS!!!
                            event.packet = ClientboundBlockUpdatePacket(
                                updatePacket.pos,
                                if (isBedrockClient) customBlock.fallbackBlockStateNMS else customBlock.targetBlockStateNMS
                            )
                        } else {
                            m.logger.warning("I don't know any Sparkly Custom Block with ID \"$customBlockKey\"! Skipping...")
                        }
                    }
                }
                return
            }

            if (packet is ClientboundSectionBlocksUpdatePacket) {
                // println("ClientboundSectionBlocksUpdatePacket for $player is in ${Thread.currentThread()}")
                // println("Multi block change")
                // This is the CHUNK SECTION POSITION
                val sectionBlocksUpdatePacket = packet
                // println("ClientboundSectionBlocksUpdatePacket for $player is $packet in ${Thread.currentThread()}, isBedrockClient? $isBedrockClient")

                val sectionPos = sectionBlocksUpdatePacket.sectionPos
                val origin = sectionPos.origin()

                val shorts = sectionBlocksUpdatePacket.positions
                // println(blockPosition.x.toString() + ", " + blockPosition.y + ", " + blockPosition.z)

                // println("Section Pos: ${sectionPos.x()}, ${sectionPos.y()}, ${sectionPos.z()}")
                // println("Shorts:")
                // shorts.forEach {
                //     val x = it.toInt() ushr 8 and 15
                //     val y = it.toInt() ushr 0 and 15
                //     val z = it.toInt() ushr 4 and 15
                //     // println("$it $x, $y, $z")
                // }

                val changedBlocks = sectionBlocksUpdatePacket.states

                // If there isn't any note blocks in this packet, let's ignore it
                if (changedBlocks.all { it.bukkitMaterial != Material.CHISELED_BOOKSHELF && it.bukkitMaterial != Material.TARGET })
                    return

                val newChangedBlockStatesWithPosition = Short2ObjectArrayMap<BlockState>(shorts.size)

                for ((index, originalBlockState) in changedBlocks.withIndex()) {
                    // TODO: Refactor this, maybe merge with the single block update code?
                    if (originalBlockState.bukkitMaterial == Material.TARGET) {
                        newChangedBlockStatesWithPosition.put(shorts[index], TARGET_BLOCK_DEFAULT_STATE)
                    } else if (originalBlockState.bukkitMaterial == Material.CHISELED_BOOKSHELF) {
                        val blockPositionRelativeToTheSection = shorts[index]

                        val x = (blockPositionRelativeToTheSection.toInt() ushr 8 and 15) + origin.x
                        val y = (blockPositionRelativeToTheSection.toInt() ushr 0 and 15) + origin.y
                        val z = (blockPositionRelativeToTheSection.toInt() ushr 4 and 15) + origin.z

                        val blockInWorld = playerWorld.getBlockAt(x, y, z).state as ChiseledBookshelf
                        val customBlockKey = blockInWorld.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY)

                        if (customBlockKey != null) {
                            // println("WrapperPlayServerBlockChange IS CUSTOM BLOCK!")

                            val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)
                            if (customBlock != null) {
                                // Oh no, NMS!!!
                                newChangedBlockStatesWithPosition.put(shorts[index], if (isBedrockClient) customBlock.fallbackBlockStateNMS else customBlock.targetBlockStateNMS)
                            } else {
                                m.logger.warning("I don't know any Sparkly Custom Block with ID \"$customBlockKey\"! Skipping...")
                            }
                        }
                    } else newChangedBlockStatesWithPosition.put(shorts[index], originalBlockState) // add the block state as is to the list
                }

                event.packet = ClientboundSectionBlocksUpdatePacket(
                    sectionPos,
                    newChangedBlockStatesWithPosition
                )
                return
            }
        }
    }
}