package net.perfectdreams.dreamraspadinha.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamraspadinha.DreamRaspadinha
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamraspadinha.commands.ScratchCardCommand
import net.perfectdreams.dreamraspadinha.tables.Raspadinhas
import net.perfectdreams.dreamraspadinha.utils.RaspadinhaHolder
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class InventoryListener(val m: DreamRaspadinha) : Listener {
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder

        if (holder !is RaspadinhaHolder)
            return

        val item = e.currentItem
        if (item == null || item.type == Material.AIR)
            return

        e.isCancelled = true

        val raspadinhaChar = item.getStoredMetadata("raspadinhaChar") ?: return

        fun transformToItemStack(char: Char): ItemStack {
            return when (char) {
                'L' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lLoritta")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.LORITTA_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    }
                    .meta<Damageable> {
                        this.damage = 4
                    }
                'P' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lPantufa")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.LORITTA_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    }
                    .meta<Damageable> {
                        this.damage = 5
                    }
                'B' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lGabriela")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.GABI_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    }
                    .meta<Damageable> {
                        this.damage = 6
                    }
                'D' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lDokyo")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.DOKYO_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    }
                    .meta<Damageable> {
                        this.damage = 7
                    }
                'G' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lGessy")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.GESSY_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    }
                    .meta<Damageable> {
                        this.damage = 8
                    }
                'T' -> ItemStack(Material.WOODEN_HOE)
                    .rename("§a§lTobias")
                    .lore("§7Ao conseguir um combo de 3 emojis, você irá ganhar §a§l${ScratchCardCommand.TOBIAS_COMBO} sonhos§7!")
                    .meta<ItemMeta> {
                        this.isUnbreakable = true
                    }
                    .meta<Damageable> {
                        this.damage = 9
                    }
                else -> throw RuntimeException("I don't know what emote is for $char")
            }
        }

        holder.scratchedCount++
        val player = e.whoClicked as Player
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.6f + (holder.scratchedCount * 0.1f))
        e.currentItem = transformToItemStack(raspadinhaChar[0])

        if (holder.scratchedCount == 9) {
            // give rewards if needed
            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                val raspadinha = transaction(Databases.databaseNetwork) {
                    Raspadinhas.select {
                        Raspadinhas.id eq holder.raspadinhaId
                    }.first()
                }

                val array = Array(3) { Array<Char>(3, init = { 'Z' }) }

                val splittedStoredPattern = raspadinha[Raspadinhas.pattern].split("\n")

                for ((y, lines) in splittedStoredPattern.withIndex()) {
                    for ((x, char) in lines.withIndex()) {
                        array[x][y] = char
                    }
                }

                fun checkArrayFor(ch: Char): Int {
                    var combos = 0
                    if (array[0][0] == ch && array[1][0] == ch && array[2][0] == ch) // horizontal, primeira linha
                        combos++
                    if (array[0][1] == ch && array[1][1] == ch && array[2][1] == ch) // horizontal, segunda linha
                        combos++
                    if (array[0][2] == ch && array[1][2] == ch && array[2][2] == ch) // horizontal, terceira linha
                        combos++

                    if (array[0][0] == ch && array[0][1] == ch && array[0][2] == ch) // vertical, primeira linha
                        combos++
                    if (array[1][0] == ch && array[1][1] == ch && array[1][2] == ch) // vertical, segunda linha
                        combos++
                    if (array[2][0] == ch && array[2][1] == ch && array[2][2] == ch) // vertical, terceira linha
                        combos++

                    if (array[0][0] == ch && array[1][1] == ch && array[2][2] == ch) // diagonal1
                        combos++
                    if (array[2][0] == ch && array[1][1] == ch && array[0][2] == ch) // diagonal2
                        combos++

                    return combos
                }

                var prize = 0
                val loriCombos = checkArrayFor('L')
                val pantufaCombos = checkArrayFor('P')
                val gabiCombos = checkArrayFor('B')
                val dokyoCombos = checkArrayFor('D')
                val gessyCombos = checkArrayFor('G')
                val tobiasCombos = checkArrayFor('T')

                prize += (loriCombos * ScratchCardCommand.LORITTA_COMBO)
                prize += (pantufaCombos * ScratchCardCommand.PANTUFA_COMBO)
                prize += (gabiCombos * ScratchCardCommand.GABI_COMBO)
                prize += (dokyoCombos * ScratchCardCommand.DOKYO_COMBO)
                prize += (gessyCombos * ScratchCardCommand.GESSY_COMBO)
                prize += (tobiasCombos * ScratchCardCommand.TOBIAS_COMBO)

                transaction(Databases.databaseNetwork) {
                    Raspadinhas.update({ Raspadinhas.id eq holder.raspadinhaId }) {
                        it[scratched] = true
                        it[value] = prize
                    }
                }

                switchContext(SynchronizationContext.SYNC)

                if (prize != 0) {
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }

                waitFor(45)

                player.closeInventory()
                if (prize == 0) {
                    player.sendMessage("${DreamRaspadinha.PREFIX} §cQue pena, você não ganhou nada...")
                } else {
                    player.balance += prize
                    player.sendMessage("${DreamRaspadinha.PREFIX} §aParabéns, você ganhou $prize sonhos!")
                }
            }
        }
    }
}