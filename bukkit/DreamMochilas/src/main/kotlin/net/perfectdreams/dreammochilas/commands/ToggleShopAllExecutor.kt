package net.perfectdreams.dreammochilas.commands

import com.Acrobot.Breeze.Utils.InventoryUtil
import com.Acrobot.ChestShop.Configuration.Properties
import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import com.Acrobot.ChestShop.Signs.ChestShopSign
import net.perfectdreams.dreamcore.utils.DefaultFontInfo
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.isPrimaryThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.listeners.InventoryListener
import net.perfectdreams.dreammochilas.utils.MochilaInventoryHolder
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.event.block.Action
import java.lang.reflect.InvocationTargetException

class ToggleShopAllExecutor(private val m: DreamMochilas) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(ToggleShopAllExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val clickedBlock = player.getTargetBlock(5)!! // TODO: handle this
        val contents = player.inventory.contents!! // TODO: update paperweight bundle

        val backpacksInPlayerInventory = contents.filterNotNull().filter {
            it.getStoredMetadata("isMochila")?.toBoolean() ?: return@filter false
            it.getStoredMetadata("mochilaId")?.toLong() ?: return@filter false

            true
        }.map { it.getStoredMetadata("mochilaId")!!.toLong() to it }

        if (backpacksInPlayerInventory.isEmpty()) {
            context.sendMessage("§cVocê não tem uma mochila no seu inventário!")
            return
        }

        if (ChestShopSign.isValid(clickedBlock)) {
            val state = clickedBlock.state as Sign

            // Do not allow interacting with the sign if it is the owner of the sign
            // While not having this doesn't seem to cause any issues (the owner buys items from themselves),
            // it is better to have this as a "better be safe than sorry" measure
            if (ChestShopSign.isOwner(player, state))
                return

            m.launchAsyncThread {

                // We are going to replace the cooldown with OUR OWN cooldown, haha!!
                // The reason we check it here instead of checking after the mochila is loaded is to avoid loading mochilas from the database/cache every time
                /* val lastTimeUserInteractedWithThis = mochilasCooldown[player] ?: 0
                val diff = System.currentTimeMillis() - lastTimeUserInteractedWithThis

                if (Properties.SHOP_INTERACTION_INTERVAL > diff && !player.hasPermission("dreammochilas.bypasscooldown")) {
                    m.logger.info { "Player ${player.name} tried selling but it was during a cooldown! Backpack ID: $mochilaId" }
                    return@launchAsyncThread
                }

                mochilasCooldown[player] = System.currentTimeMillis() */

                for ((mochilaId, itemStack) in backpacksInPlayerInventory) {
                    m.logger.info { "Player ${player.name} is doing transaction! Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                    val triggerType = "${player.name} bulk buying/selling stuff"
                    val mochilaAccessHolder = MochilaUtils.retrieveMochilaAndHold(mochilaId, triggerType)
                    if (mochilaAccessHolder == null) {
                        player.sendMessage("§cEssa mochila não existe!")
                        return@launchAsyncThread
                    }

                    val inventory = mochilaAccessHolder.getOrCreateMochilaInventoryAndHold()

                    val status = onMainThread {
                        val sign = clickedBlock.state as Sign
                        try {
                            m.logger.info { "Preparing Pre Transaction Event for ${player.name}... Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                            val r = InventoryListener.preparePreTransactionEventMethod.invoke(null as Any?, sign, player, Action.LEFT_CLICK_BLOCK) as PreTransactionEvent

                            r.clientInventory = inventory
                            // TODO: this is a hack
                            r.setStock(
                                *InventoryUtil.getItemsStacked(
                                    r.stock
                                        .first()
                                        .asQuantity(2304)
                                )
                            )
                            // We need to remove from the spam click protector because, if we don't, it will just ignore the event
                            InventoryListener.chestShopSpamClickProtectorMap.remove(player)

                            Bukkit.getPluginManager().callEvent(r)

                            if (r.isCancelled) {
                                m.logger.info { "Pre Transaction Event for ${player.name} was cancelled! ${r.transactionType} ${r.transactionOutcome}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }
                                return@onMainThread false
                            }

                            val tEvent = TransactionEvent(r, sign)

                            Bukkit.getPluginManager().callEvent(tEvent)

                            if (tEvent.isCancelled) {
                                m.logger.info { "Transaction Event for ${player.name} was cancelled! ${tEvent.transactionType}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }
                                return@onMainThread false
                            }

                            m.logger.info { "Transaction Event for ${player.name} was successfully completed! ${tEvent.transactionType}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                            return@onMainThread true
                        } catch (var8: SecurityException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: IllegalAccessException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: IllegalArgumentException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: InvocationTargetException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: NoSuchMethodException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        }
                    }

                    m.logger.info { "Player ${player.name} transaction finished! Holding mochila locks... Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId; Status: $status" }
                    // Releasing locks...
                    (inventory.holder as MochilaInventoryHolder).accessHolders.poll()
                        ?.release(triggerType)
                    onMainThread {
                        MochilaUtils.updateMochilaItemLore(inventory, itemStack)
                    }
                }
            }
        } else {
            context.sendMessage("§cVocê não está olhando para uma placa de venda!")
        }
    }
}