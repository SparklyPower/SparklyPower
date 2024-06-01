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

object PicaretaMonstraCommand : DSLCommandBase<DreamPicaretaMonstra> {
    override fun command(plugin: DreamPicaretaMonstra) = create(
        listOf("picaretamonstra")
    ) {
        permission = "sparklypower.picaretamonstra"

        executes {
            val picaretaMonstra = ItemStack(Material.DIAMOND_PICKAXE)
                .rename("§6§lPicareta Monstra")
                .lore("§6Tá saindo da jaula o monstro!")
                .meta<ItemMeta> {
                    setCustomModelData(1)
                    persistentDataContainer.set(DreamPicaretaMonstra.IS_MONSTER_TOOL_KEY, true)
                }

            player.inventory.addItem(picaretaMonstra)
        }
    }
}