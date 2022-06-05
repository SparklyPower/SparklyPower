package net.perfectdreams.dreamenchant.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamenchant.DreamEnchant
import org.bukkit.block.Block
import org.bukkit.block.EnchantingTable
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class SpawnEnchantmentTable(val m: DreamEnchant) : SpecialEnchantmentTable() {
    private val SPAWN_ENCHANTMENT_TABLE_CREDITS = SparklyNamespacedKey("spawn_enchantment_table_credits")

    override val creditsAmount = 3

    override fun openEnchantmentInventoryOrCreditsScreen(player: Player, clickedBlock: Block, page: Int) {
        getCreditsMapFromSpawnEnchantmentTable(clickedBlock) { spawnEnchantmentTableCredits ->
            val currentCredits = spawnEnchantmentTableCredits[player.uniqueId.toString()] ?: 0

            if (currentCredits == 0) {
                // The user doesn't have any credits!
                openEnchantmentCreditsInventory(player, clickedBlock, page)
            } else {
                // They do have credits!
                openEnchantmentInventory(player, clickedBlock, page, currentCredits)
            }
        }
    }

    override fun hasEnoughCreditsToPurchaseEnchantment(player: Player, block: Block): Boolean {
        return getCreditsMapFromSpawnEnchantmentTable(block) { spawnEnchantmentTableCredits ->
            val currentCredits = spawnEnchantmentTableCredits[player.uniqueId.toString()] ?: 0
            if (currentCredits == 0)
                return@getCreditsMapFromSpawnEnchantmentTable false

            spawnEnchantmentTableCredits[player.uniqueId.toString()] = currentCredits - 1

            return@getCreditsMapFromSpawnEnchantmentTable true
        }
    }

    private fun openEnchantmentInventory(player: Player, clickedBlock: Block, page: Int, credits: Int) {
        val heldItem = player.inventory.itemInMainHand

        if (!validateIfItemCanBeEnchanted(player, heldItem))
            return

        val menu = createEnchantmentsMenu(
            m,
            player,
            heldItem,
            clickedBlock,
            page,
            credits,
            this::openEnchantmentInventory,
            this::openEnchantmentCreditsInventory
        )

        menu.sendTo(player)
    }

    private fun openEnchantmentCreditsInventory(player: Player, clickedBlock: Block, page: Int) {
        val menu = createEnchantmentsCreditsMenu(
            m,
            player,
            clickedBlock,
            page
        ) { player, clickedBlock ->
            // Increase the credits count
            getCreditsMapFromSpawnEnchantmentTable(clickedBlock) { spawnEnchantmentTableCredits ->
                val currentCredits = spawnEnchantmentTableCredits[player.uniqueId.toString()] ?: 0
                spawnEnchantmentTableCredits[player.uniqueId.toString()] = currentCredits + creditsAmount
            }
        }

        menu.sendTo(player)
    }

    private fun <T> getCreditsMapFromSpawnEnchantmentTable(block: Block, callback: (MutableMap<String, Int>) -> (T)): T {
        val blockState = block.state as EnchantingTable
        val persistentDataStorage = blockState.persistentDataContainer

        // Hacky, but it works!
        val spawnEnchantmentTableCreditsJson = persistentDataStorage.get(SPAWN_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.STRING)
        val spawnEnchantmentTableCredits = if (spawnEnchantmentTableCreditsJson != null)
            Json.decodeFromString<Map<String, Int>>(spawnEnchantmentTableCreditsJson).toMutableMap()
        else mutableMapOf()

        val currHashCode = spawnEnchantmentTableCredits.hashCode()

        val result = callback.invoke(spawnEnchantmentTableCredits)

        // Update if the current hash code != new hash code
        if (currHashCode != spawnEnchantmentTableCredits.hashCode()) {
            persistentDataStorage.set(SPAWN_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.STRING, Json.encodeToString(spawnEnchantmentTableCredits))
            blockState.update()
        }

        return result
    }
}