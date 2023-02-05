package net.perfectdreams.dreamsocial.gui.profile.settings.preferences.helper.item

import net.perfectdreams.dreamcore.utils.extensions.asBoldComponent
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun BroadcastType.item(): ItemStack {
    val customModelData = when(this) {
        BroadcastType.PLAYER_ANNOUNCEMENT -> 104
        BroadcastType.CHAT_EVENT -> 98
        BroadcastType.EVENT_ANNOUNCEMENT -> 99
        BroadcastType.GAMBLING_MESSAGE -> 100
        BroadcastType.JETPACK_MESSAGE -> 101
        BroadcastType.LOGIN_ANNOUNCEMENT -> 102
        BroadcastType.PRIVATE_MESSAGE -> 103
        BroadcastType.SERVER_ANNOUNCEMENT -> 97
        BroadcastType.THANOS_SNAP -> 105
        BroadcastType.VOTES_MESSAGE -> 106
    }

    return ItemStack(Material.PAPER).meta<ItemMeta> {
        displayName(componentName)
        setCustomModelData(customModelData)
    }
}