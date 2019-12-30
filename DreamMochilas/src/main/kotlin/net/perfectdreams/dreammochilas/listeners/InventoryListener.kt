package net.perfectdreams.dreammochilas.listeners

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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InventoryListener(val m: DreamMochilas) : Listener {
    val savingMochilas = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())
    val savingUntrackedMochilas = Collections.newSetFromMap(ConcurrentHashMap<Player, Boolean>())
    val loadedMochilaInventories = ConcurrentHashMap<Long, Inventory>()

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        savingUntrackedMochilas.remove(e.player)
    }

    @EventHandler
    fun onOpen(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        if (e.item?.type == Material.CARROT_ON_A_STICK) {
            val isMochila = e.item.getStoredMetadata("isMochila")?.toBoolean() ?: return
            val mochilaId = e.item.getStoredMetadata("mochilaId")?.toLong()

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
                            this.type = (e.item.itemMeta as Damageable).damage
                            this.funnyId = funnyId
                        }
                    }

                    switchContext(SynchronizationContext.SYNC)

                    var item = e.item

                    val position = e.player.inventory.first(e.item)
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