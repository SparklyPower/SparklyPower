package net.perfectdreams.dreampicaretamonstra.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object PaMonstraCommand : DSLCommandBase<DreamPicaretaMonstra> {
    override fun command(plugin: DreamPicaretaMonstra) = create(
        listOf("pamonstra")
    ) {
        permission = "sparklypower.pamonstra"

        executes {
            val paMonstra = ItemStack(Material.DIAMOND_SHOVEL)
                .rename("§6§lPá Monstra")
                .lore("§6Tá saindo da jaula o monstro!")
                .meta<ItemMeta> {
                    setCustomModelData(1)
                    this.persistentDataContainer.set(DreamPicaretaMonstra.IS_MONSTER_TOOL_KEY, true)
                }

            player.inventory.addItem(paMonstra)
        }
    }
}