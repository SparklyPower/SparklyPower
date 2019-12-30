package net.perfectdreams.dreamcorrida.utils

import org.bukkit.Location

class Corrida(val name: String, val spawn: LocationWrapper) {
    val checkpoints = mutableListOf<Checkpoint>()
    var ready = false
}