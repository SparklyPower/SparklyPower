package net.perfectdreams.dreamcustomitems.utils

import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.get
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.persistence.PersistentDataType

object CustomBlocks {
    val CUSTOM_BLOCK_KEY = SparklyNamespacedKey("custom_block", PersistentDataType.STRING)

    val RAINBOW_WOOL = CustomBlock(
        "rainbow_wool",
        {
            it.type == Material.WHITE_WOOL && it.hasItemMeta() && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 1
        },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 1
        } as CraftBlockData,
        Bukkit.createBlockData(Material.RED_WOOL)
    ) {
        listOf(CustomItems.RAINBOW_WOOL)
    }

    val RAINBOW_CONCRETE = CustomBlock(
        "rainbow_concrete",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "rainbow_concrete"
        },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 3
        } as CraftBlockData,
        Bukkit.createBlockData(Material.RED_CONCRETE)
    ) {
        listOf(CustomItems.RAINBOW_CONCRETE)
    }

    val RAINBOW_TERRACOTTA = CustomBlock(
        "rainbow_terracotta",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "rainbow_terracotta"
        },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 4
        } as CraftBlockData,
        Bukkit.createBlockData(Material.RED_TERRACOTTA)
    ) {
        listOf(CustomItems.RAINBOW_TERRACOTTA)
    }

    val ASPHALT_SERVER = CustomBlock(
        "asphalt_server",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_server"
        },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 2
        } as CraftBlockData,
        Bukkit.createBlockData(Material.BLACK_CONCRETE)
    ) {
        emptyList()
    }

    val ASPHALT_PLAYER = CustomBlock(
        "asphalt_player",
        {
            it.hasItemMeta() && it.itemMeta.persistentDataContainer.get(CustomItems.CUSTOM_ITEM_KEY) == "asphalt_player"
        },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 2
        } as CraftBlockData,
        Bukkit.createBlockData(Material.BLACK_CONCRETE)
    ) {
        listOf(CustomItems.ASPHALT_PLAYER)
    }

    val TEST_BLOCK = CustomBlock(
        "test",
        { false },
        Bukkit.createBlockData(Material.TARGET) {
            it as AnaloguePowerable
            it.power = 2
        } as CraftBlockData,
        Bukkit.createBlockData(Material.BLACK_CONCRETE)
    ) {
        listOf(CustomItems.RUBY)
    }

    val allCustomBlocks = listOf(
        RAINBOW_WOOL,
        RAINBOW_CONCRETE,
        RAINBOW_TERRACOTTA,
        ASPHALT_SERVER,
        ASPHALT_PLAYER,
        TEST_BLOCK
    )

    private val customBlocks = buildMap {
        for (customBlock in allCustomBlocks) {
            put(customBlock.id, customBlock)
        }
    }

    fun getCustomBlockOfBlock(block: Block): CustomBlock? {
        if (block.type != Material.CHISELED_BOOKSHELF)
            return null

        val blockState = block.state as? ChiseledBookshelf ?: return null
        val customBlockKey = blockState.persistentDataContainer.get(CustomBlocks.CUSTOM_BLOCK_KEY) ?: return null
        val customBlock = CustomBlocks.getCustomBlockById(customBlockKey)
        return customBlock
    }

    fun getCustomBlockById(id: String) = customBlocks[id]
}