package net.perfectdreams.dreammochilas.listeners

import com.Acrobot.ChestShop.Events.PreTransactionEvent
import com.Acrobot.ChestShop.Events.TransactionEvent
import com.Acrobot.ChestShop.Listeners.Player.PlayerInteract
import com.Acrobot.ChestShop.Listeners.PreTransaction.SpamClickProtector
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.FunnyIds
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InventoryListener(val m: DreamMochilas) : Listener {
    companion object {
        val savingMochilas = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())
        val savingUntrackedMochilas = Collections.newSetFromMap(ConcurrentHashMap<Player, Boolean>())
        val loadedMochilaInventories = ConcurrentHashMap<Long, Inventory>()
        val trackingMochilasPreTransactionsEvents = Collections.newSetFromMap(ConcurrentHashMap<PreTransactionEvent, Boolean>())
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onMochilaPreTransaction(e: PreTransactionEvent) {
        if (trackingMochilasPreTransactionsEvents.contains(e)) {
            trackingMochilasPreTransactionsEvents.remove(e)
            return
        }

        val item = e.client.inventory.itemInMainHand

        if (item.type == Material.CARROT_ON_A_STICK && e.transactionOutcome == PreTransactionEvent.TransactionOutcome.TRANSACTION_SUCCESFUL) {
            val isMochila = item.getStoredMetadata("isMochila")?.toBoolean() ?: return
            val mochilaId = item.getStoredMetadata("mochilaId")?.toLong()

            if (!isMochila)
                return

            // É uma mochila, iremos cancelar o evento
            e.isCancelled = true

            // E agora iremos pegar na db a mochila... como é async, a gente "cancela"
            if (savingMochilas.contains(mochilaId))
                return

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                val mochila = transaction(Databases.databaseNetwork) {
                    Mochila.find { Mochilas.id eq mochilaId }
                        .firstOrNull()
                }

                if (mochila == null) {
                    e.client.sendMessage("§cEssa mochila não existe!")
                    return@schedule
                }

                switchContext(SynchronizationContext.SYNC)

                try {
                    val sign = e.sign
                    val player = e.client

                    val action = if (e.transactionType == TransactionEvent.TransactionType.BUY) {
                        Action.RIGHT_CLICK_BLOCK
                    } else {
                        Action.LEFT_CLICK_BLOCK
                    }

                    val r = preparePreTransactionEventMethod.invoke(null as Any?, sign, player, action) as PreTransactionEvent?
                        ?: return@schedule

                    val mochilaInventory = mochila.createMochilaInventory()
                    r.clientInventory = mochilaInventory

                    trackingMochilasPreTransactionsEvents.add(r)
                    chestShopSpamClickProtectorMap.remove(player)
                    Bukkit.getPluginManager().callEvent(r)

                    if (r.isCancelled)
                        return@schedule

                    val tEvent = TransactionEvent(r, sign)
                    Bukkit.getPluginManager().callEvent(tEvent)

                    if (tEvent.isCancelled)
                        return@schedule

                    // Não foi cancelado, então vamos salvar o conteúdo dela!
                    val base64Mochila = mochilaInventory.toBase64(1)

                    // Para evitar que alguém possa abrir ANTES de ter salvado, vamos adicionar em um set
                    savingMochilas.add(mochila.id.value)
                    loadedMochilaInventories.remove(mochila.id.value)

                    switchContext(SynchronizationContext.ASYNC)

                    scheduler().schedule(m, SynchronizationContext.ASYNC) {
                        transaction(Databases.databaseNetwork) {
                            mochila.content = base64Mochila
                        }

                        savingMochilas.remove(mochila.id.value)
                    }
                } catch (var8: SecurityException) {
                    var8.printStackTrace()
                } catch (var8: IllegalAccessException) {
                    var8.printStackTrace()
                } catch (var8: IllegalArgumentException) {
                    var8.printStackTrace()
                } catch (var8: InvocationTargetException) {
                    var8.printStackTrace()
                } catch (var8: NoSuchMethodException) {
                    var8.printStackTrace()
                }
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        savingUntrackedMochilas.remove(e.player)
    }

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
                if (savingUntrackedMochilas.contains(e.player))
                    return

                savingUntrackedMochilas.add(e.player)

                scheduler().schedule(m, SynchronizationContext.ASYNC) {
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

                    switchContext(SynchronizationContext.SYNC)

                    var item = item

                    val position = e.player.inventory.first(item)
                    if (position == -1) // wait what?
                        return@schedule

                    item = item.lore(
                        "§7Mochila de §b${e.player.name}",
                        "§7",
                        "§6$funnyId"
                    ).storeMetadata("mochilaId", mochila.id.value.toString())

                    e.player.inventory.setItem(position, item)

                    if (loadedMochilaInventories[mochila.id.value] != null) {
                        e.player.openInventory(loadedMochilaInventories[mochila.id.value]!!)
                        return@schedule
                    }

                    val inventory = mochila.createMochilaInventory()

                    loadedMochilaInventories[mochila.id.value] = inventory

                    e.player.openInventory(inventory)

                    savingUntrackedMochilas.remove(e.player)
                }
                return
            }

            if (savingMochilas.contains(mochilaId))
                return

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                val mochila = transaction(Databases.databaseNetwork) {
                    Mochila.find { Mochilas.id eq mochilaId }
                        .firstOrNull()
                }

                if (mochila == null) {
                    e.player.sendMessage("§cEssa mochila não existe!")
                    return@schedule
                }

                switchContext(SynchronizationContext.SYNC)

                if (loadedMochilaInventories[mochila.id.value] != null) {
                    e.player.openInventory(loadedMochilaInventories[mochila.id.value]!!)
                    return@schedule
                }

                val inventory = mochila.createMochilaInventory()

                loadedMochilaInventories[mochila.id.value] = inventory

                e.player.openInventory(inventory)
            }
        }
    }

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

            val base64Mochila = e.inventory.toBase64(1)

            // Para evitar que alguém possa abrir ANTES de ter salvado, vamos adicionar em um set
            savingMochilas.add(holder.mochila.id.value)
            loadedMochilaInventories.remove(holder.mochila.id.value)

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                transaction(Databases.databaseNetwork) {
                    holder.mochila.content = base64Mochila
                }

                savingMochilas.remove(holder.mochila.id.value)
            }
        }
    }

    @EventHandler
    fun onMove(e: InventoryClickEvent) {
        // Não deixar colocar a mochila dentro da mochila
        val mochilaId = e.currentItem?.getStoredMetadata("mochilaId")?.toLong() ?: return

        val holder  = e.inventory?.holder

        if (holder is Mochila.MochilaHolder) {
            if (holder.mochila.id.value == mochilaId) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onMove(e: InventoryMoveItemEvent) {
        // Não deixar colocar a mochila dentro da mochila
        val mochilaId = e.item?.getStoredMetadata("mochilaId")?.toLong() ?: return

        val holder  = e.destination?.holder

        if (holder is Mochila.MochilaHolder) {
            if (holder.mochila.id.value == mochilaId) {
                e.isCancelled = true
            }
        }
    }
}