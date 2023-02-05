package net.perfectdreams.dreamsocial.gui.profile

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.extensions.asBoldComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

enum class ProfileLayout(
    private val char: Char,
    private val showcaseModelData: Int,
    private val panelModelData: Int,
    private val obtainableInLootBoxes: Boolean
) {
    LORITTA('\uE267', 121, 124, false),
    PANTUFA('\uE268', 122, 125, false),
    GABRIELA('\uE269', 123, 124, false);

    private val formattedName = this.name.lowercase().replaceFirstChar(Char::uppercase)
    val menuTitle = "ꈉ§f${this.char}§r陇"

    val showcaseItem = paperWithCustomModelData(this.showcaseModelData)
    val panelItem = paperWithCustomModelData(this.panelModelData)

    private fun paperWithCustomModelData(customModelData: Int) =
        ItemStack(Material.PAPER).meta<ItemMeta> {
            setCustomModelData(customModelData)
            displayName((if (customModelData == panelModelData) " " else formattedName).asBoldComponent.color(NamedTextColor.AQUA))
        }

    companion object {
        val freeToUseLayouts = values().filterNot(ProfileLayout::obtainableInLootBoxes)
    }
}