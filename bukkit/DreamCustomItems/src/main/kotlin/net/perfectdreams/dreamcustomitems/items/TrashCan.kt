package net.perfectdreams.dreamcustomitems.items

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Location
import org.bukkit.entity.Player

class TrashCan(val m: DreamCustomItems, val location: Location) {

    fun open(player: Player) {
        player.playSound(location, "perfectdreams.sfx.trashcan.open", 1f, 1f)
        player.performCommand("lixeira")
    }
}