package net.perfectdreams.dreamcustomitems.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import net.perfectdreams.dreamcustomitems.utils.LegacyCustomBlocks
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent

// This is used for legacy custom blocks, should be removed in the future!
class LegacyCustomBlocksListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        // If this is a custom block, we need to drop it!
        if (m.getCustomBlocksInWorld(e.block.world.name).contains(BlockPosition.fromBlock(e.block))) {
            val blockData = e.block.state.blockData as NoteBlock
            val block = LegacyCustomBlocks.allCustomBlocks.firstOrNull {
                blockData.instrument == it.instrument && blockData.note.id.toInt() == it.note
            } ?: return

            // Drop the item!
            e.isDropItems = false
            e.block.world.dropItem(e.block.location.toCenterLocation(), block.sourceItem)
        }

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

        // Toggle sneak
        val sneakingStateBefore = e.player.isSneaking
        e.player.isSneaking = true

        m.schedule {
            // Reset sneak after 1 tick
            waitFor(1L)

            e.player.isSneaking = sneakingStateBefore
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onNotePlay(e: NotePlayEvent) {
        // Disallow Note Block Note Play
        if (m.getCustomBlocksInWorld(e.block.world.name).contains(BlockPosition.fromBlock(e.block)))
            e.isCancelled = true
    }
}
