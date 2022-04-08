package net.perfectdreams.dreamxizum.battle.npcs

import net.minecraft.world.entity.EquipmentSlot
import net.perfectdreams.dreamcore.utils.createNPC
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

object Corpse {
    fun createDeadBody(player: Player, location: Location) = createNPC("", location.toFloorLevel()) {
        skin {
            player.playerProfile.properties.first().also {
                texture = it.value
                signature = it.signature.toString()
            }
        }
    }.apply {
        player.inventory.armorContents?.filterNotNullTo(mutableSetOf())?.let { equipment = it }
        changeItem(player.inventory.itemInMainHand, EquipmentSlot.MAINHAND)
        isSleeping = true
    }

    private fun Location.toFloorLevel(): Location {
        while (block.type == Material.AIR) { y-- }
        return apply { y++ }
    }
}