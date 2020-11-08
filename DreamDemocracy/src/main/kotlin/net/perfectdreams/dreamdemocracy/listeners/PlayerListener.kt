package net.perfectdreams.dreammusically.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreammusically.DreamMusically
import net.perfectdreams.dreammusically.utils.MusicPack
import org.bukkit.Material
import org.bukkit.SoundCategory
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

                // Uma gambiarra, já que Spigot não deixa colocar itens que não sejam records dentro de jukeboxes
                val nmsWorld = (e.player.world as org.bukkit.craftbukkit.v1_16_R3.CraftWorld).getHandle() as net.minecraft.server.v1_16_R3.World
                val tileEntity = nmsWorld.getTileEntity(net.minecraft.server.v1_16_R3.BlockPosition(clickedBlock.x, clickedBlock.y, clickedBlock.z))

                val copyOfItemInMainHand = e.player.inventory.itemInMainHand.clone()

                m.schedule {
                    waitFor(1)
                    val nmsJukebox = tileEntity as net.minecraft.server.v1_16_R3.TileEntityJukeBox
                    nmsWorld.setTypeAndData(
                        nmsJukebox.getPosition(),
                        net.minecraft.server.v1_16_R3.Blocks.JUKEBOX.getBlockData().set(
                            net.minecraft.server.v1_16_R3.BlockJukeBox.HAS_RECORD,
                            true
                        ) as net.minecraft.server.v1_16_R3.IBlockData,
                        3
                    )

                    nmsJukebox.setRecord(org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack.asNMSCopy(copyOfItemInMainHand))
                    e.player.inventory.setItemInMainHand(copyOfItemInMainHand.clone().apply { this.amount-- })
                    e.player.world.playSound(clickedBlock.location, customPlayingSong.play, SoundCategory.RECORDS, 4f, 1f)

                    clickedBlock.world.players.filter { 4096 >= it.location.distanceSquared(clickedBlock.location) }.forEach {
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