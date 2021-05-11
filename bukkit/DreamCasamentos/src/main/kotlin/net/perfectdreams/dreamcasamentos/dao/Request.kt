package net.perfectdreams.dreamcasamentos.dao

import org.bukkit.entity.Player

class Request(
        val type: RequestKind,
        val sender: Player,
        val target: Player
) {
    enum class RequestKind {
        MARRIAGE,
        ADOPTION
    }
}