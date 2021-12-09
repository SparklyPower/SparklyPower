package net.perfectdreams.dreammochilas.listeners

import com.Acrobot.ChestShop.Configuration.Properties.SHOP_INTERACTION_INTERVAL
import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import com.Acrobot.ChestShop.Listeners.Player.PlayerInteract
import com.Acrobot.ChestShop.Listeners.PreTransaction.SpamClickProtector
import com.Acrobot.ChestShop.Signs.ChestShopSign
import com.sk89q.worldguard.bukkit.util.Events
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.FunnyIds
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InventoryListener(val m: DreamMochilas) : Listener {
    companion object {
        val preparePreTransactionEventMethod by lazy {
            PlayerInteract::class.java.getDeclaredMethod(
                "preparePreTransactionEvent",
                Sign::class.java,
                Player::class.java,
                Action::class.java
            ).apply {
                this.isAccessible = true
            }
        }
        val chestShopSpamClickProtectorMap by lazy {
            val protector = PreTransactionEvent.getHandlerList().registeredListeners.first {
                it.listener::class.java == SpamClickProtector::class.java
            }.listener

            SpamClickProtector::class.java.getDeclaredField(
                "TIME_OF_LATEST_CLICK"
            ).apply {
                this.isAccessible = true
            }.get(protector) as WeakHashMap<Player, Long>
        }
    }

    val mochilasCooldown = ConcurrentHashMap<Player, Long>()

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        mochilasCooldown.remove(e.player)
    }

    @InternalCoroutinesApi
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        val item = e.player.inventory.itemInMainHand

        val isMochila = item.getStoredMetadata("isMochila")?.toBoolean() ?: return
        val mochilaId = item.getStoredMetadata("mochilaId")?.toLong() ?: return

        if (!isMochila)
            return

        // Gigantic workaround
        val isSign = clickedBlock.type.name.endsWith("_SIGN")

        if (!isSign)
            return

        // If it is a mochila, just stop here right there
        e.isCancelled = true

        if (ChestShopSign.isValid(e.clickedBlock)) {
            val state = clickedBlock.state as Sign

            // Do not allow interacting with the sign if it is the owner of the sign
            // While not having this doesn't seem to cause any issues (the owner buys items from themselves),
            // it is better to have this as a "better be safe than sorry" measure
            if (ChestShopSign.isOwner(e.player, state))
                return

            m.launchAsyncThread {
                m.logger.info { "Player ${e.player.name} is doing transaction, is mutex locked? Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                val triggerType = "${e.player.name} buying/selling stuff"
                val mochila = MochilaUtils.retrieveMochila(mochilaId, triggerType)

                if (mochila == null) {
                    e.player.sendMessage("§cEssa mochila não existe!")
                    // Uuuuuh, what are you doing then?
                    return@launchAsyncThread
                }

                val triggerMochilaSave = mochila.lockForInventoryManipulation {
                    onMainThread {
                        val sign = clickedBlock.state as Sign
                        try {
                            m.logger.info { "Preparing Pre Transaction Event for ${e.player.name}... Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                            val r = preparePreTransactionEventMethod.invoke(null as Any?, sign, e.player, e.action) as PreTransactionEvent

                            r.clientInventory = mochila.getOrCreateMochilaInventory()
                            // We need to remove from the spam click protector because, if we don't, it will just ignore the event
                            chestShopSpamClickProtectorMap.remove(e.player)

                            Bukkit.getPluginManager().callEvent(r)

                            // We are going to replace the cooldown with OUR OWN cooldown, haha!!
                            val lastTimeUserInteractedWithThis = mochilasCooldown[e.player] ?: 0
                            val diff = System.currentTimeMillis() - lastTimeUserInteractedWithThis

                            if (diff > SHOP_INTERACTION_INTERVAL) {
                                m.logger.info { "Player ${e.player.name} tried selling but it was during a cooldown! Backpack ID: $mochilaId" }
                                r.setCancelled(PreTransactionEvent.TransactionOutcome.SPAM_CLICKING_PROTECTION)
                                return@onMainThread false
                            }

                            if (r.isCancelled) {
                                m.logger.info { "Pre Transaction Event for ${e.player.name} was cancelled! ${r.transactionType} ${r.transactionOutcome}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }
                                return@onMainThread false
                            }

                            val tEvent = TransactionEvent(r, sign)

                            Bukkit.getPluginManager().callEvent(tEvent)

                            if (tEvent.isCancelled) {
                                m.logger.info { "Transaction Event for ${e.player.name} was cancelled! ${tEvent.transactionType}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }
                                return@onMainThread false
                            }

                            m.logger.info { "Transaction Event for ${e.player.name} was successfully completed! ${tEvent.transactionType}; Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId" }

                            mochilasCooldown[e.player] = System.currentTimeMillis()

                            return@onMainThread true
                        } catch (var8: SecurityException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: IllegalAccessException) {
                            var8.printStackTrace()
                            return@onMainThread false
                        } catch (var8: java.lang.IllegalArgumentException) {
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
                }

                m.logger.info { "Player ${e.player.name} transaction finished! Is thread async? ${!isPrimaryThread}; Backpack ID: $mochilaId; Result: $triggerMochilaSave" }

                if (triggerMochilaSave) {
                    MochilaUtils.saveMochila(item, mochila, triggerType)
                } else {
                    MochilaUtils.removeCachedMochila(mochila, triggerType)
                }
            }
        }
    }

    @InternalCoroutinesApi
    @EventHandler
    fun onOpen(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        // ChestShop, não iremos processar caso o cara esteja clicando em placas
        if (e.clickedBlock?.type?.name?.contains("SIGN") == true)
            return

        val item = e.item

        if (item?.type == Material.CARROT_ON_A_STICK) {
            val isMochila = item.getStoredMetadata("isMochila")?.toBoolean() ?: return
            val mochilaId = item.getStoredMetadata("mochilaId")?.toLong()

            if (!isMochila)
                return

            e.isCancelled = true

            if (mochilaId == null) { // Criar mochila, caso ainda não tenha um ID associado a ela
                val position = e.player.inventory.first(item)
                if (position == -1) // wait what?
                    return

                m.launchAsyncThread {
                    MochilaUtils.mochilaCreationMutex.withLock {
                        val newInventory = Bukkit.createInventory(null, 27, "Mochila")
                        val funnyId = FunnyIds.generatePseudoId()

                        newInventory.addItem(
                            ItemStack(Material.PAPER)
                                .rename("§a§lBem-Vind" + MeninaAPI.getArtigo(e.player) + ", §e§l" + e.player.displayName + "§a§l!")
                                .lore(
                                    "§7Gostou da sua nova Mochila?",
                                    "§7Aqui você pode guardar qualquer item que você quiser!",
                                    "§7Você pode comprar mais mochilas para ter mais espaço!",
                                    "§7",
                                    "§c§lCuidado!",
                                    "§cSe você perder esta mochila,",
                                    "§cvocê irá perder todos os itens que estão dentro dela!"
                                )
                        )

                        val mochila = transaction(Databases.databaseNetwork) {
                            Mochila.new {
                                this.owner = e.player.uniqueId
                                this.size = 27
                                this.content = (newInventory.toBase64(1))
                                this.type = (item.itemMeta as Damageable).damage
                                this.funnyId = funnyId
                            }
                        }

                        // Handle just like a normal mochila would
                        // Should NEVER be null
                        val loadedFromDatabaseMochila = MochilaUtils.retrieveMochila(mochila.id.value, "${e.player.name} mochila creation")!!

                        val inventory = loadedFromDatabaseMochila.getOrCreateMochilaInventory()
                        onMainThread {
                            var item = item

                            item = item.lore(
                                "§7Mochila de §b${e.player.name}",
                                "§7",
                                "§6$funnyId"
                            ).storeMetadata("mochilaId", mochila.id.value.toString())

                            e.player.inventory.setItem(position, item)

                            e.player.openInventory(inventory)
                        }
                    }
                }
                return
            }

            m.launchAsyncThread {
                val mochila = MochilaUtils.retrieveMochila(mochilaId, "${e.player.name} mochila opening")

                onMainThread {
                    if (mochila == null) {
                        e.player.sendMessage("§cEssa mochila não existe!")
                        return@onMainThread
                    }

                    if (e.player.openInventory.topInventory.type != InventoryType.CRAFTING) {
                        m.logger.warning { "Player ${e.player.name} tried opening a backpack when they already had a inventory open! ${e.player.openInventory.topInventory.type} Backpack ID: ${mochila.id.value}" }
                        return@onMainThread
                    }

                    m.logger.info { "Player ${e.player.name} opened a backpack. Backpack ID: ${mochila.id.value}" }

                    val inventory = mochila.getOrCreateMochilaInventory()

                    e.player.openInventory(inventory)
                }
            }
        }
    }

    @InternalCoroutinesApi
    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val holder = e.inventory.holder

        if (holder is Mochila.MochilaHolder && e.inventory.viewers.size == 1) { // Nenhuma pessoa está vendo o inventário, então vamos salvar!
            // Antes de fechar, vamos verificar se a mochila está dentro da mochila
            val closingMochilaId = holder.mochila.id.value
            for (idx in 0 until e.inventory.size) {
                val item = e.inventory.getItem(idx) ?: continue

                val mochilaId = item.getStoredMetadata("mochilaId")?.toLong()

                if (mochilaId == closingMochilaId) { // omg
                    m.logger.warning("Player ${e.player.name} is trying to close $mochilaId while the backpack is within itself! Giving item to player...")
                    if (e.player.inventory.canHoldItem(item))
                        e.player.inventory.addItem(item)
                    else
                        e.player.world.dropItem(e.player.location, item)
                    e.inventory.clear(idx)
                }
            }

            m.launchAsyncThread {
                val item = e.player.inventory.itemInMainHand

                val isMochila = item.getStoredMetadata("isMochila")?.toBoolean()

                MochilaUtils.saveMochila(
                    if (isMochila == true) item else null // Maybe the user changed the held item?
                    , holder.mochila, "${e.player.name} mochila inventory close")
            }
        }
    }

    @EventHandler
    fun onMove(e: InventoryClickEvent) {
        // Não deixar colocar a mochila dentro da mochila
        val mochilaId = e.currentItem?.getStoredMetadata("mochilaId")?.toLong() ?: return

        val holder = e.inventory.holder

        if (holder is Mochila.MochilaHolder) {
            if (holder.mochila.id.value == mochilaId) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onMove(e: InventoryMoveItemEvent) {
        // Não deixar colocar a mochila dentro da mochila
        val mochilaId = e.item.getStoredMetadata("mochilaId")?.toLong() ?: return

        val holder = e.destination.holder

        if (holder is Mochila.MochilaHolder) {
            if (holder.mochila.id.value == mochilaId) {
                e.isCancelled = true
            }
        }
    }
}