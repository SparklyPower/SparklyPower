package net.perfectdreams.dreamcustomitems.blocks

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.*
import com.comphenix.protocol.wrappers.WrappedBlockData
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType
import com.viaversion.viaversion.api.type.types.version.ChunkSectionType1_18
import io.netty.buffer.Unpooled
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData
import sun.misc.Unsafe
import java.lang.reflect.Field
import kotlin.math.absoluteValue


class BlockPacketAdapter(val m: DreamCustomItems) : PacketAdapter(
    m,
    ListenerPriority.NORMAL, // Listener priority
    listOf(
        PacketType.Play.Server.MAP_CHUNK,
        PacketType.Play.Server.BLOCK_CHANGE,
        PacketType.Play.Server.MULTI_BLOCK_CHANGE,
    ),
    ListenerOptions.ASYNC
) {
    companion object {
        val chunkSectionType = ChunkSectionType1_18(Block.BLOCK_STATE_REGISTRY.size(), Registry.BIOME_SOURCE.size())
    }

    override fun onPacketSending(event: PacketEvent) {
        // println(event.packetType)
        val packet = event.packet

        // I hope this is correct ;w; (concurrent API access oof)
        val playerWorld = event.player.world

        if (event.packetType == PacketType.Play.Server.MAP_CHUNK) {
            // This is actually pretty weird
            // there is actually a "nested" (?) packet
            // and because ProtocolLib reads the non-nested packet, we need to cast to the NMS object and read it from there
            //
            // Very useful! https://nms.screamingsandals.org/
            val clientboundLevelChunkWithLightPacket = event.packet.handle as ClientboundLevelChunkWithLightPacket
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

                // Quick fail: Only edit if the palette contains note blocks
                val hasNoteBlockInPalette = (0 until blockPalette.size()).any {
                    val blockId = blockPalette.idByIndex(it)
                    Block.BLOCK_STATE_REGISTRY.byId(blockId)?.bukkitMaterial == Material.NOTE_BLOCK
                }

                // Does not have any note blocks in the block palette...
                if (!hasNoteBlockInPalette)
                    continue

                var hasNoteBlock = false

                // Replace all note blocks with default note block data
                for (y in 0 until 16) {
                    for (x in 0 until 16) {
                        for (z in 0 until 16) {
                            val blockId = section.getFlatBlock(x, y, z)

                            val blockData = Block.BLOCK_STATE_REGISTRY.byId(blockId)

                            if (blockData != null) {
                                if (blockData.bukkitMaterial == Material.NOTE_BLOCK) {
                                    // Okay, so it is a note block... but what if it is a *custom* block?
                                    val position = BlockPosition(
                                        coordinateX + x,
                                        y + (16 * i) - worldMinHeight.absoluteValue, // The sections are from the bottom to the top, so the section 0 is at the world's min height!
                                        coordinateZ + z
                                    )

                                    // println("Note Block!")
                                    // println("Coordinate X: ${position.x}")
                                    // println("Coordinate Y: ${position.y}")
                                    // println("Coordinate Z: ${position.z}")

                                    val isCustomBlock = m.getCustomBlocksInWorld(playerWorld.name).contains(position)

                                    // If it is a custom block, leave the block as is :3
                                    if (isCustomBlock) {
                                        // println("Custom Note Block!")
                                        // println("Coordinate X: ${position.x}")
                                        // println("Coordinate Y: ${position.y}")
                                        // println("Coordinate Z: ${position.z}")
                                        continue
                                    }

                                    // println("Not-custom Note Block!")
                                    // println("Coordinate X: ${position.x}")
                                    // println("Coordinate Y: ${position.y}")
                                    // println("Coordinate Z: ${position.z}")

                                    section.setFlatBlock(
                                        x,
                                        y,
                                        z,
                                        Block.BLOCK_STATE_REGISTRY.getId(Blocks.NOTE_BLOCK.defaultBlockState())
                                    )
                                    hasNoteBlock = true
                                }
                            }
                        }
                    }
                }

                if (!requiresEdits)
                    requiresEdits = hasNoteBlock
            }

            if (requiresEdits) {
                // println("Requires edit, so we are going to clear the read buffer")
                // Only rewrite the packet if we really need to edit the packet
                val byteBuf = Unpooled.buffer()
                sections.forEach {
                    chunkSectionType.write(byteBuf, it)
                }

                writeByteArrayDataToLevelChunkDataPacket(
                    chunkDataPacket,
                    byteBuf.array()
                )
            }
        } else if (event.packetType == PacketType.Play.Server.BLOCK_CHANGE) {
            val wrapper = WrapperPlayServerBlockChange(event.packet)

            if (wrapper.blockData.type == Material.NOTE_BLOCK) {
                // So, we are changing a note block? Interesting...

                // If it is a custom block, just leave it as is :3
                if (m.getCustomBlocksInWorld(playerWorld.name).contains(
                        BlockPosition(
                            wrapper.location.x,
                            wrapper.location.y,
                            wrapper.location.z
                        )
                    )
                ) {
                    // println("Block Update is a custom block, we will not change it...")
                    return
                }

                // Oh no, NMS!!!
                wrapper.blockData = WrappedBlockData.createData(CraftBlockData.fromData(Blocks.NOTE_BLOCK.defaultBlockState()))
            }

            event.packet = wrapper.handle
        } else if (event.packetType == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            // println("Multi block change")
            // This is the CHUNK SECTION POSITION
            val blockPosition = event.packet.sectionPositions.read(0)

            // println(blockPosition.x.toString() + ", " + blockPosition.y + ", " + blockPosition.z)
            val shorts = event.packet.shortArrays.read(0)

            /* println("Shorts:")
            shorts.forEach {
                val x = it.toInt() ushr 8 and 15
                val y = it.toInt() ushr 0 and 15
                val z = it.toInt() ushr 4 and 15
                println("$it $x, $y, $z")
            } */

            val changedBlocks = event.packet.blockDataArrays.read(0)

            // If there isn't any note blocks in this packet, let's ignore it
            if (changedBlocks.all { it.type != Material.NOTE_BLOCK })
                return

            // Sadly PacketWrapper isn't updated yet...
            val array = mutableListOf<WrappedBlockData>()

            // If there's any note blocks in this packet...
            changedBlocks.forEachIndexed { index, it ->
                // println(it.handle)
                // println(it.handle::class.java)

                if (it.type == Material.NOTE_BLOCK) {
                    val blockPositionRelativeToTheSection = shorts[index]

                    val x = (blockPositionRelativeToTheSection.toInt() ushr 8 and 15) + blockPosition.x
                    val y = (blockPositionRelativeToTheSection.toInt() ushr 0 and 15) + blockPosition.y
                    val z = (blockPositionRelativeToTheSection.toInt() ushr 4 and 15) + blockPosition.z

                    // println("Update coords: $x; $y; $z")

                    if (m.getCustomBlocksInWorld(playerWorld.name).contains(
                            BlockPosition(
                                x,
                                y,
                                z
                            )
                        )
                    ) {
                        // println("Multi Block Update is a custom block, we will keep it as is...")
                        array.add(it)
                        return
                    }

                    // Create a default note block
                    val craftBlockData = Bukkit.createBlockData(Material.NOTE_BLOCK) as CraftBlockData

                    // And add a wrapped block data!
                    array.add(WrappedBlockData.fromHandle(craftBlockData.state))
                } else {
                    // Keep as it if it isn't a note block
                    array.add(it)
                }
            }

            // And write the new array to the packet
            event.packet.blockDataArrays.write(0, array.toTypedArray())
        }
    }

    private fun writeByteArrayDataToLevelChunkDataPacket(packet: ClientboundLevelChunkPacketData, data: ByteArray) {
        val unsafeField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe: Unsafe = unsafeField.get(null) as Unsafe

        // buffer = c, see https://nms.screamingsandals.org/1.18/net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData.html
        val ourField: Field = ClientboundLevelChunkPacketData::class.java.getDeclaredField("c")
        val staticFieldOffset: Long = unsafe.objectFieldOffset(ourField)
        unsafe.putObject(packet, staticFieldOffset, data)
    }
}