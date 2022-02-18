package net.perfectdreams.dreamchestshopstuff.listeners

import com.Acrobot.Breeze.Utils.InventoryUtil
import com.Acrobot.ChestShop.Events.PreShopCreationEvent
import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import com.Acrobot.ChestShop.Listeners.Modules.StockCounterModule
import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.Acrobot.ChestShop.Signs.ChestShopSign.ITEM_LINE
import com.Acrobot.ChestShop.Utils.uBlock
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamchestshopstuff.DreamChestShopStuff
import net.perfectdreams.dreamcore.tables.EventVictories.event
import net.perfectdreams.dreamcore.utils.DefaultFontInfo
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.awt.Color
import kotlin.math.sign

class ColorizeShopSignsListener(val m: DreamChestShopStuff) : Listener {
    companion object {
        private val COLOR_LOGO_RED = ChatColor.of(Color(237, 46, 22))
        private val COLOR_LOGO_AQUA = ChatColor.of(Color(1, 235, 247))
    }

    // https://github.com/ChestShop-authors/ChestShop-3/issues/503
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPreShopCreation(event: PreShopCreationEvent) {
        val isAdminShop = ChestShopSign.isAdminShop(ChatColor.stripColor(event.getSignLine(0)))
        if (isAdminShop) {
            event.signLines = buildColoredSign(event.signLines.toList(), true, true).toTypedArray()
            return
        }

        // Admin Shops do not have an inventory, so let's just... not do that
        val itemTradedByShop = StockCounterModule.determineItemTradedByShop(event.getSignLine(ITEM_LINE))
        val chestShopInventory = uBlock.findConnectedContainer(event.sign).inventory
        val numTradedItemsInChest = InventoryUtil.getAmount(itemTradedByShop, chestShopInventory)
        val hasSpaceInChest = InventoryUtil.fits(itemTradedByShop, chestShopInventory)

        event.signLines = buildColoredSign(
            event.signLines.toList(),
            numTradedItemsInChest != 0,
            hasSpaceInChest
        ).toTypedArray()
        return
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventoryLocation = event.inventory.location
        if (inventoryLocation == null || !ChestShopSign.isShopBlock(inventoryLocation.block))
            return

        for (shopSign in uBlock.findConnectedShopSigns(event.inventory.holder)) {
            if (ChestShopSign.isAdminShop(shopSign))
                return

            updateSignColorBasedOnStockQuantity(shopSign, event.inventory)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onTransaction(event: PreTransactionEvent) {
        // Used to retroactively apply the new stock counter to old signs
        if (event.transactionOutcome == PreTransactionEvent.TransactionOutcome.NOT_ENOUGH_STOCK_IN_CHEST) {
            for (shopSign in uBlock.findConnectedShopSigns(event.ownerInventory.holder)) {
                updateSignColorBasedOnStockQuantity(shopSign, event.ownerInventory)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onTransaction(event: TransactionEvent) {
        if (ChestShopSign.isAdminShop(event.sign)) {
            // Admin Shops return null for the "event.ownerInventory.holder", so will only update the sign that is provided in the event
            updateSignColorBasedOnStockQuantity(event.sign, event.ownerInventory)
        } else {
            for (shopSign in uBlock.findConnectedShopSigns(event.ownerInventory.holder)) {
                updateSignColorBasedOnStockQuantity(shopSign, event.ownerInventory)
            }
        }
    }

    // From the StockCounterModule class
    private fun updateSignColorBasedOnStockQuantity(sign: Sign, chestShopInventory: Inventory?) {
        val isAdminShop = ChestShopSign.isAdminShop(ChatColor.stripColor(sign.getLine(0)))
        if (!isAdminShop && chestShopInventory != null) {
            val itemTradedByShop: ItemStack = StockCounterModule.determineItemTradedByShop(sign) ?: return
            val numTradedItemsInChest: Int = InventoryUtil.getAmount(itemTradedByShop, chestShopInventory)
            val hasSpaceInChest = InventoryUtil.fits(itemTradedByShop, chestShopInventory)

            buildColoredSign(
                (0 until 4).map { sign.getLine(it) },
                numTradedItemsInChest != 0,
                hasSpaceInChest
            ).forEachIndexed { index, s ->
                sign.setLine(index, s)
            }

            // TODO: We could check if the sign really needs to be updated, instead of calling the update method every time
            sign.update(true)
            return
        }

        buildColoredSign(
            (0 until 4).map { sign.getLine(it) },
            true,
            true
        ).forEachIndexed { index, s ->
            sign.setLine(index, s)
        }

        // TODO: We could check if the sign really needs to be updated, instead of calling the update method every time
        sign.update(true)
    }

    private fun buildColoredSign(
        lines: List<String>,
        hasStock: Boolean,
        hasSpaceInChest: Boolean
    ): List<String> {
        val newLines = lines.map { ChatColor.stripColor(it) }
            .toMutableList()
        val ownerLine = newLines[0]

        // This checks if the sign has stock OR if the sign is not selling items, and then does the same thing for buying items
        // Useful because a sign may have stock
        if ((!hasStock || !newLines[2].contains("B")) && (!hasSpaceInChest || !newLines[2].contains("S"))) {
            return newLines.map {
                "§c$it"
            }
        }

        // Items in the chest, update it to be our custom ChestShop sign color!
        // Owner
        if (ChestShopSign.isAdminShop(ownerLine)) {
            newLines[0] = "${COLOR_LOGO_RED}§lSparkly${COLOR_LOGO_AQUA}§lShop"
        } else {
            newLines[0] = "§1" + newLines[0]
        }

        // Price
        newLines[2] = newLines[2]

        if (newLines[2].contains("B")) {
            if (hasStock) {
                newLines[2] = newLines[2].replace("B", "§aB")
            } else {
                newLines[2] = newLines[2].replace("B", "§cB")
            }
        }

        if (newLines[2].contains("S")) {
            if (hasSpaceInChest) {
                newLines[2] = newLines[2].replace("S", "§4S")
            } else {
                newLines[2] = newLines[2].replace("S", "§cS")
            }
        }

        newLines[2] = newLines[2].replace(":", "§0:")

        // Item
        newLines[3] = "§9" + newLines[3]

        return newLines
    }
}