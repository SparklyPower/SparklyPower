package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Material
import org.bukkit.block.data.Levelled
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class RemoveDisabledPluginsRecipesListener(val m: DreamCustomItems) : Listener {
    @EventHandler
    fun onDisable(e: PluginDisableEvent) {
        m.customRecipes.removeAll { e.plugin == it.plugin }
    }
}