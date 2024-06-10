package net.perfectdreams.dreamcore.utils.effects

import com.comphenix.packetwrapper.WrapperPlayServerSetSlot
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object CustomTotemRessurectEffect {
    /**
     * Sends a custom totem animation with the texture of a totem with a [customModelDataId]
     *
     * This uses packets to replace the current off hand held item with a custom totem, sends the effect and then
     * updates the player inventory to remove the fake totem item.
     *
     * @param player            the player that will receive the totem animation
     * @param customModelDataId the model ID of the custom totem
     */
    fun sendCustomTotemAnimation(player: Player, customModelDataId: Int) =
        sendCustomTotemAnimation(
            player,
            ItemStack(Material.TOTEM_OF_UNDYING).meta<ItemMeta> {
                setCustomModelData(customModelDataId)
            }
        )

    /**
     * Sends a custom totem animation with the texture of a totem with a [customModelDataId]
     *
     * This uses packets to replace the current off hand held item with a custom totem, sends the effect and then
     * updates the player inventory to remove the fake totem item.
     *
     * @param player    the player that will receive the totem animation
     * @param itemStack the ItemStack that will be set in the player's offhand. Needs to be a [Material.TOTEM_OF_UNDYING].
     */
    fun sendCustomTotemAnimation(player: Player, itemStack: ItemStack) {
        if (itemStack.type != Material.TOTEM_OF_UNDYING)
            throw IllegalArgumentException("ItemStack $itemStack isn't a Totem of Undying!")

        val packet = ClientboundContainerSetSlotPacket(
            0,
            0,
            45,
            CraftItemStack.unwrap(itemStack)
        )

        (player as CraftPlayer).handle.connection.send(packet)

        player.playEffect(EntityEffect.TOTEM_RESURRECT)
        player.updateInventory()
    }
}