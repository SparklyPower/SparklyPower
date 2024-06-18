package net.perfectdreams.dreamcustomitems.utils

import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.get
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.type.Slab
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.persistence.PersistentDataType

object CustomBlocks {
    val CUSTOM_BLOCK_KEY = SparklyNamespacedKey("custom_block", PersistentDataType.STRING)

    val RAINBOW_WOOL = CustomBlock(
        "rainbow_wool",
        {
            it.type == Material.WHITE_WOOL && it.hasItemMeta() && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 1
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_RAINBOW_WOOL),
                Bukkit.createBlockData(Material.TARGET) {
                    it as AnaloguePowerable
                    it.power = 1
                } as CraftBlockData,
                Bukkit.createBlockData(Material.RED_WOOL)
            ) { listOf(CustomItems.RAINBOW_WOOL) }
        )
    )

    val RAINBOW_CONCRETE = CustomBlock(
        "rainbow_concrete",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "rainbow_concrete"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_RAINBOW_CONCRETE),
                Bukkit.createBlockData(Material.TARGET) {
                    it as AnaloguePowerable
                    it.power = 3
                } as CraftBlockData,
                Bukkit.createBlockData(Material.RED_CONCRETE)
            ) {
                listOf(CustomItems.RAINBOW_CONCRETE)
            }
        )
    )

    val RAINBOW_TERRACOTTA = CustomBlock(
        "rainbow_terracotta",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "rainbow_terracotta"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_RAINBOW_TERRACOTTA),
                Bukkit.createBlockData(Material.TARGET) {
                    it as AnaloguePowerable
                    it.power = 4
                } as CraftBlockData,
                Bukkit.createBlockData(Material.RED_TERRACOTTA)
            ) {
                listOf(CustomItems.RAINBOW_TERRACOTTA)
            }
        )
    )

    // For the server, the asphalt block and slab are different blocks (just like how normal slabs works), but for the client they are just petrified oak slabs
    // This way we can differentiate between them, and drop slabs correctly
    val ASPHALT_SERVER = CustomBlock(
        "asphalt_server",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_server"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_SERVER),
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACK_CONCRETE)
            ) { emptyList() }
        )
    )

    val ASPHALT_SERVER_SLAB = CustomBlock(
        "asphalt_server_slab",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_server_slab"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_SERVER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                }
            ) { emptyList() },
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_SERVER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                }
            ) { emptyList() },
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_SERVER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                }
            ) { emptyList() }
        )
    )

    val ASPHALT_PLAYER = CustomBlock(
        "asphalt_player",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_player"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_PLAYER),
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACK_CONCRETE)
            ) { emptyList(CustomItems.ASPHALT_PLAYER) }
        )
    )

    val ASPHALT_PLAYER_SLAB = CustomBlock(
        "asphalt_player_slab",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_player_slab"
        },
        listOf(
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_PLAYER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.BOTTOM
                }
            ) { listOf(CustomItems.ASPHALT_PLAYER) },
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_PLAYER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.TOP
                }
            ) { listOf(CustomItems.ASPHALT_PLAYER) },
            CustomBlock.CustomBlockState(
                Bukkit.createBlockData(Material.SPARKLYPOWER_ASPHALT_PLAYER_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                },
                Bukkit.createBlockData(Material.PETRIFIED_OAK_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                } as CraftBlockData,
                Bukkit.createBlockData(Material.BLACKSTONE_SLAB) {
                    it as Slab
                    it.type = Slab.Type.DOUBLE
                }
            ) { listOf(CustomItems.ASPHALT_PLAYER, CustomItems.ASPHALT_PLAYER) }
        )
    )

    val allCustomBlocks = listOf(
        RAINBOW_WOOL,
        RAINBOW_CONCRETE,
        RAINBOW_TERRACOTTA,
        ASPHALT_SERVER,
        ASPHALT_SERVER_SLAB,
        ASPHALT_PLAYER,
        ASPHALT_PLAYER_SLAB,
    )

    private val materialToCustomBlock = buildMap {
        for (customBlock in allCustomBlocks) {
            for (state in customBlock.blockStates) {
                put(state.sourceBlockData.material, state)
            }
            }
    }

    private val customBlocks = buildMap {
        for (customBlock in allCustomBlocks) {
            put(customBlock.id, customBlock)
        }
    }

    val stateToBlockState = buildMap {
        for (customBlock in allCustomBlocks) {
            for (state in customBlock.blockStates) {
                put(state.sourceBlockState, state)
            }
        }
    }

    val stateToNMSBlockState = buildMap {
        for (customBlock in allCustomBlocks) {
            for (state in customBlock.blockStates) {
                put(state.sourceBlockStateNMS, state)
            }
        }
    }

    val stateIdToBlockState = buildMap {
        for (customBlock in allCustomBlocks) {
            for (state in customBlock.blockStates) {
                put(state.sourceBlockStateId, state)
            }
        }
    }

    val customBlocksThatUseTargetBlockAsATarget = allCustomBlocks.flatMap { it.blockStates }.filter {
        it.targetBlockData.material == Material.TARGET
    }

    fun getCustomBlockOfBlock(block: Block) = getCustomBlockOfMaterial(block.type)

    fun getCustomBlockOfMaterial(material: Material) = materialToCustomBlock[material]

    fun getCustomBlockOfState(blockState: BlockState) = stateToBlockState[blockState]

    fun getCustomBlockOfNMSState(blockState: net.minecraft.world.level.block.state.BlockState) = stateToNMSBlockState[blockState]

    fun getCustomBlockById(id: String) = customBlocks[id]
}