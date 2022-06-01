package net.perfectdreams.dreamenchant.utils

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.isMagnet
import net.perfectdreams.dreamenchant.DreamEnchant
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.EnchantingTable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import protocolsupport.api.TranslationAPI

class PlayerEnchantmentTable(val m: DreamEnchant) : SpecialEnchantmentTable() {
    companion object {
        val SUPER_ENCHANTMENT_TABLE_CREDITS = SparklyNamespacedKey("super_enchantment_table_credits")
    }

    override val creditsAmount = 6

    override fun openEnchantmentInventoryOrCreditsScreen(player: Player, clickedBlock: Block, page: Int) {
        getCreditsMapFromSuperEnchantmentTable(clickedBlock) { currentCredits ->
            if (currentCredits == 0) {
                // The user doesn't have any credits!
                openEnchantmentCreditsInventory(player, clickedBlock, page)
            } else {
                // They do have credits!
                openEnchantmentInventory(player, clickedBlock, page, currentCredits)
            }

            return@getCreditsMapFromSuperEnchantmentTable Pair(currentCredits, Unit)
        }
    }

    override fun hasEnoughCreditsToPurchaseEnchantment(player: Player, block: Block): Boolean {
        return getCreditsMapFromSuperEnchantmentTable(block) { currentCredits ->
            if (currentCredits == 0)
                return@getCreditsMapFromSuperEnchantmentTable Pair(currentCredits, false)

            return@getCreditsMapFromSuperEnchantmentTable Pair(currentCredits - 1, true)
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
        val claim = GriefPrevention.instance.dataStore.getClaimAt(clickedBlock.location, false, null)

        if (claim != null && (claim.ownerName != player.name && claim.allowContainers(player) != null)) {
            player.sendMessage("§cVocê não tem permissão para carregar créditos nesta super mesa de encantamento!")
            return
        }

        val menu = createEnchantmentsCreditsMenu(
            m,
            player,
            clickedBlock,
            page
        ) { player, clickedBlock ->
            // Increase the credits count
            getCreditsMapFromSuperEnchantmentTable(clickedBlock) { currentCredits ->
                return@getCreditsMapFromSuperEnchantmentTable Pair(currentCredits + creditsAmount, Unit)
            }
        }


        menu.sendTo(player)
    }

    private fun <T> getCreditsMapFromSuperEnchantmentTable(block: Block, callback: (Int) -> (Pair<Int, T>)): T {
        val blockState = block.state as EnchantingTable
        val persistentDataStorage = blockState.persistentDataContainer

        val credits = persistentDataStorage.get(SUPER_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.INTEGER) ?: 0

        val (newCredits, t) = callback.invoke(credits)

        if (newCredits != credits) {
            persistentDataStorage.set(SUPER_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.INTEGER, newCredits)
            blockState.update()
        }

        return t
    }
}