package net.perfectdreams.dreampicaretamonstra.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object PaMonstraCommand : DSLCommandBase<DreamPicaretaMonstra> {
    override fun command(plugin: DreamPicaretaMonstra) = create(
            listOf("pamonstra")
    ) {
        permission = "sparklypower.pamonstra"

        executes {
            val paMonstra = ItemStack(Material.DIAMOND_SHOVEL).storeMetadata("isMonsterPickaxe", "true")
                    .rename("§6§lPá Monstra")
                    .lore("§6Tá saindo da jaula o monstro!")

            player.inventory.addItem(paMonstra)
        }
    }
}