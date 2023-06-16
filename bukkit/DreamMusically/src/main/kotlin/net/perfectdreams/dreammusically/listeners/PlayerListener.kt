package net.perfectdreams.dreammusically.listeners

import com.okkero.skedule.schedule
import kotlinx.coroutines.delay
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.JukeboxBlock
import net.minecraft.world.level.block.entity.JukeboxBlockEntity
import net.minecraft.world.level.gameevent.GameEvent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreammusically.DreamMusically
import net.perfectdreams.dreammusically.utils.MusicPack
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_20_R1.block.CraftJukebox
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class PlayerListener(val m: DreamMusically) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        val clickedBlock = e.block

        if (clickedBlock.type != Material.JUKEBOX)
            return

        // e.player.sendMessage(e.action.toString())
        var state = clickedBlock.state as org.bukkit.block.Jukebox

        val jukebox = clickedBlock.state as org.bukkit.block.Jukebox

        if (jukebox.playing != Material.AIR) { // Se tiver algo dentro da Jukebox...
            // e.player.sendMessage("Não está vazio...")
            if (jukebox.playing == Material.CARROT_ON_A_STICK) { // E for um music pack...
                // e.player.sendMessage("E é um carrot on a sticc!")
                val customPlayingSong = MusicPack.musicPacks.firstOrNull { it.damage == (jukebox.record.itemMeta as org.bukkit.inventory.meta.Damageable).damage } ?: return

                clickedBlock.world.players.filter { 4096 >= it.location.distanceSquared(clickedBlock.location) }.forEach {
                    // e.player.sendMessage("Parando som para ${it}")
                    it.stopSound(customPlayingSong.play, SoundCategory.RECORDS)
                }
            }
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return

        if (clickedBlock.type != Material.JUKEBOX)
            return

        val jukebox = clickedBlock.state as org.bukkit.block.Jukebox

        if (jukebox.playing != Material.AIR) { // Se tiver algo dentro da Jukebox...
            // e.player.sendMessage("Não está vazio...")
            if (jukebox.playing == Material.CARROT_ON_A_STICK) { // E for um music pack...
                // e.player.sendMessage("E é um carrot on a sticc!")
                val customPlayingSong = MusicPack.musicPacks.firstOrNull { it.damage == (jukebox.record.itemMeta as org.bukkit.inventory.meta.Damageable).damage } ?: return

                clickedBlock.world.players.filter { 4096 >= it.location.distanceSquared(clickedBlock.location) }.forEach {
                    // e.player.sendMessage("Parando som para ${it}")
                    it.stopSound(customPlayingSong.play, SoundCategory.RECORDS)
                }
            }
            return
        }

        val itemInPlayerMainHand = e.player.inventory.itemInMainHand ?: return

        if (itemInPlayerMainHand.type == Material.CARROT_ON_A_STICK) {
            val customPlayingSong = MusicPack.musicPacks.firstOrNull { it.damage == (itemInPlayerMainHand.itemMeta as org.bukkit.inventory.meta.Damageable).damage } ?: return

            if (customPlayingSong != null) {
                val jukebox = clickedBlock.state as org.bukkit.block.Jukebox
                if (jukebox.playing != Material.AIR) // ignore se não for ar
                    return

                val copyOfItemInMainHand = e.player.inventory.itemInMainHand.clone()

                // Cancel the event to avoid the block being updated
                e.isCancelled = true

                // Uma gambiarra, já que Spigot não deixa colocar itens que não sejam records dentro de jukeboxes
                // Even tho you *should* be able to set the record via the .setRecord function, it doesn't change the item within the Jukebox if it
                // isn't a record.
                // (Last tested: 1.20)
                val nmsWorld = (e.player.world as org.bukkit.craftbukkit.v1_20_R1.CraftWorld).handle
                val tileEntity = nmsWorld.getBlockEntity(BlockPos(clickedBlock.x, clickedBlock.y, clickedBlock.z))

                // Yes this delay is required, if not the record will pop out after putting it in
                m.launchMainThread {
                    delay(1)
                    val nmsJukebox = tileEntity as JukeboxBlockEntity
                    val level = nmsJukebox.level ?: error("NMS Jukebox does not have a level!")

                    // Update record and set that there is a record within it
                    nmsJukebox.setRecordWithoutPlaying(
                        org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(
                            copyOfItemInMainHand
                        )
                    )
                    level.setBlock(
                        nmsJukebox.blockPos,
                        nmsJukebox.blockState.setValue(JukeboxBlock.HAS_RECORD, true),
                        2
                    )
                    level.gameEvent(GameEvent.BLOCK_CHANGE, nmsJukebox.blockPos, GameEvent.Context.of(null, nmsJukebox.blockState))

                    e.player.inventory.setItemInMainHand(copyOfItemInMainHand.clone().apply { this.amount-- })
                    e.player.world.playSound(
                        clickedBlock.location,
                        customPlayingSong.play,
                        SoundCategory.RECORDS,
                        4f,
                        1f
                    )

                    clickedBlock.world.players.filter { 4096 >= it.location.distanceSquared(clickedBlock.location) }
                        .forEach {
                            e.player.sendActionBar("Tocando agora: ${customPlayingSong.name}")
                            it.stopSound(customPlayingSong.play, SoundCategory.RECORDS)
                        }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onClick(e: com.Acrobot.ChestShop.Events.ItemParseEvent) {
        val cleanItemString = org.bukkit.ChatColor.stripColor(e.itemString)!!
        if (cleanItemString.startsWith("Disco ")) {
            val itemId = cleanItemString.substring("Disco ".length)

            val damageValue = itemId?.toIntOrNull()

            if (damageValue != null) {
                val musicPack = MusicPack.musicPacks.firstOrNull { it.damage == damageValue } ?: return

                e.item = ItemStack(Material.CARROT_ON_A_STICK)
                    .meta<ItemMeta> {
                        setDisplayName("§bDisco de Música")
                        lore = listOf(
                            "§7${musicPack.name}"
                        )
                        setUnbreakable(true)
                    }.meta<org.bukkit.inventory.meta.Damageable> {
                        damage = damageValue
                    }
            }
        }
    }

}