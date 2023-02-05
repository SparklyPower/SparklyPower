package net.perfectdreams.dreamsocial.gui.confirmation

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun renderConfirmationMenu(callback: (HumanEntity) -> Unit) = createMenu(9, "ꈉ§f\uE266§r陇§fVocê tem certeza?") {
    slot(3, 0) {
        item = getConfirmationButton(true, "Confirmar" to "Cancelar")

        onClick(callback)
    }

    slot(4, 0) {
        item = ItemStack(Material.PAPER).meta<ItemMeta> {
            displayName(" ".asComponent)
            setCustomModelData(125)
        }
    }

    slot(5, 0) {
        item = getConfirmationButton(false, "Confirmar" to "Cancelar")

        onClick {
            it.closeInventory()
        }
    }
}

fun getConfirmationButton(isConfirm: Boolean, options: Pair<String, String>) =
    ItemStack(Material.PAPER, 1)
        .meta<ItemMeta> {
            displayName(
                (if (isConfirm) options.first else options.second).asComponent
                    .decorations(mapOf(
                        TextDecoration.BOLD to TextDecoration.State.TRUE,
                        TextDecoration.ITALIC to TextDecoration.State.FALSE
                    ))
                    .color(TextColor.color(if (isConfirm) 0x38CE00 else 0xF0100F))
            )

            setCustomModelData(if (isConfirm) 82 else 81)
        }