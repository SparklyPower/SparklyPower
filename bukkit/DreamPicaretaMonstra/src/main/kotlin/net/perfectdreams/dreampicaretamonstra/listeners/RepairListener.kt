package net.perfectdreams.dreampicaretamonstra.listeners

import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent
import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.meta.Damageable

class RepairListener(val m: DreamPicaretaMonstra) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onRepair(e: McMMOPlayerRepairCheckEvent) {
        if (DreamPicaretaMonstra.isMonsterTool(e.repairedObject)) {
            e.isCancelled = true
            e.player.sendMessage("§cVocê não pode reparar uma ferramenta monstra!")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSalvage(e: McMMOPlayerSalvageCheckEvent) {
        if (DreamPicaretaMonstra.isMonsterTool(e.salvageItem)) {
            e.isCancelled = true
            e.player.sendMessage("§cVocê não pode salvar uma ferramenta monstra!")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAnvilRepair(e: PrepareAnvilEvent) {
        val inventory = e.inventory
        val result = e.result
        if (result != null && DreamPicaretaMonstra.isMonsterTool(result)) {
            val item = e.inventory.firstOrNull { DreamPicaretaMonstra.isMonsterTool(it) }

            if (item != null && e.result != null && item.hasItemMeta() && result.hasItemMeta() && (item.itemMeta as Damageable).damage != (result.itemMeta as Damageable).damage) {
                // Only block repairs
                inventory.repairCost *= 16
            }
        }
    }
}