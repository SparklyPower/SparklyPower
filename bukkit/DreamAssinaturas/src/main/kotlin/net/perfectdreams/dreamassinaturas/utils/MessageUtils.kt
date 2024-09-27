package net.perfectdreams.dreamassinaturas.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player

fun Player.buildAndSendMessage(block: TextComponent.Builder.() -> Unit) {
    val builder = Component.text(block).toBuilder()

    return this.sendMessage(builder.build())
}