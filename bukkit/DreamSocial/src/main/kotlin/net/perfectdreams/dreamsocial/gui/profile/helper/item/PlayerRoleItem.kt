package net.perfectdreams.dreamsocial.gui.profile.helper

import net.perfectdreams.dreamcore.utils.extensions.PlayerRole
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun PlayerRole.item(isGirl: Boolean): ItemStack {
    var customModelData = when(this) {
        PlayerRole.MEMBER -> 112
        PlayerRole.VIP -> 115
        PlayerRole.VIP_PLUS -> 116
        PlayerRole.VIP_PLUS_PLUS -> 117
        PlayerRole.DEVELOPER -> 109
        PlayerRole.BUILDER -> 108
        PlayerRole.TRIAL_SUPPORT -> 114
        PlayerRole.SUPPORT -> 114
        PlayerRole.MODERATOR -> 111
        PlayerRole.ADMIN -> 107
        PlayerRole.OWNER -> 113
    }

    if (this == PlayerRole.MODERATOR && isGirl) customModelData--

    return ItemStack(Material.PAPER).meta<ItemMeta> {
        displayName(" ".asComponent)
        setCustomModelData(customModelData)
    }
}