package net.perfectdreams.dreamcore.utils.extensions

import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.PlayerUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

fun Player.canBreakAt(location: Location, material: Material) = PlayerUtils.canBreakAt(location, this, material)
fun Player.canPlaceAt(location: Location, material: Material) = PlayerUtils.canPlaceAt(location, this, material)

var Player.girl: Boolean
    get() = MeninaAPI.isGirl(this.uniqueId)
    set(value) {
        MeninaAPI.setGirlStatus(this.uniqueId, value)
        return
    }

val Player.pronome: String
    get() = MeninaAPI.getPronome(this)

val Player.artigo: String
    get() = MeninaAPI.getArtigo(this)