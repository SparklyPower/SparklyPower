package net.perfectdreams.dreamcustomitems.listeners

import ml.beancraft.haricot.event.block.NoteBlockUpdateEvent
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import net.perfectdreams.dreamcustomitems.utils.CustomBlocks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent

class CustomBlocksListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCustomBlockPlace(e: BlockPlaceEvent) {
        val customBlock = CustomBlocks.allCustomBlocks.firstOrNull { e.itemInHand.isSimilar(it.sourceItem) }

        if (customBlock == null) {
            // If it isn't a custom block, remove it from the custom blocks set! (Maybe the block was removed via WE and it is still in the set, who knows)
            m.getCustomBlocksInWorld(e.block.world.name).remove(BlockPosition.fromBlock(e.block))
            return
        }

        m.getCustomBlocksInWorld(e.block.world.name).add(BlockPosition.fromBlock(e.block))

        val state = e.block.state
        state.type = customBlock.targetType
        val noteBlockData = state.blockData as NoteBlock
        noteBlockData.instrument = customBlock.instrument
        noteBlockData.note = Note(customBlock.note)
        state.blockData = noteBlockData
        state.update(true, false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        m.getCustomBlocksInWorld(e.block.world.name).remove(BlockPosition.fromBlock(e.block))
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        // Allow building on top of custom blocks with note blocks without holding SHIFT
        if (e.action != Action.RIGHT_CLICK_BLOCK)
            return

        val clickedItem = e.item ?: return
        if (!clickedItem.type.isBlock) // we only care about BLOCKS
            return
        val clickedBlock = e.clickedBlock ?: return

        // This only affects blocks on top of note blocks, so whatever
        if (clickedBlock.type != Material.NOTE_BLOCK)
            return

        val whereBlockShouldBePlaced = clickedBlock.getRelative(e.blockFace)

        val oldState = whereBlockShouldBePlaced.state.blockData.clone()
        whereBlockShouldBePlaced.type = clickedItem.type
        val blockPlaceEvent = BlockPlaceEvent(
            whereBlockShouldBePlaced,
            whereBlockShouldBePlaced.state,
            whereBlockShouldBePlaced,
            clickedItem,
            e.player,
            true,
            e.hand!!
        )

        Bukkit.getPluginManager().callEvent(blockPlaceEvent)

        if (blockPlaceEvent.isCancelled) {
            // oh no... revert the changes!!
            blockPlaceEvent.block.state.blockData = oldState
            blockPlaceEvent.block.state.update(true, false)
            return
        } else {
            // Cancel the interact event because we will take the matters in our own hands!1!!
            e.isCancelled = true

            if (e.player.gameMode != GameMode.CREATIVE)
                clickedItem.amount -= 1
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNotePlay(e: NotePlayEvent) {
        // Disallow Note Block Note Play
        if (m.getCustomBlocksInWorld(e.block.world.name).contains(BlockPosition.fromBlock(e.block)))
            e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNoteBlockUpdate(e: NoteBlockUpdateEvent) {
        // Blocks random ticking/block changes updating the note block
        if (m.getCustomBlocksInWorld(e.block.world.name).contains(BlockPosition.fromBlock(e.block))) {
            e.isCancelled = true
            // Me and my homies hate client side prediction
            e.block.world.players.forEach {
                it.sendBlockChange(e.block.location, e.block.blockData)
            }
        }
    }
}