package net.perfectdreams.dreamcore.utils.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class LoreBuilder {
    internal val components = mutableListOf<Component>()

    fun textComponent(block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().apply(block).build().also { components += it }
    fun textComponent(text: String, block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().content(text).apply(block).build().also { components += it }

    fun textWithoutDecorations(block: TextComponent.Builder.() -> (Unit) = {}) = Component.text()
        .decoration(TextDecoration.ITALIC, false)
        .color(NamedTextColor.WHITE)
        .apply(block)
        .build()
        .also { components += it }
    fun textWithoutDecorations(text: String, block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().content(text)
        .decoration(TextDecoration.ITALIC, false)
        .color(NamedTextColor.WHITE)
        .apply(block)
        .build()
        .also { components += it }

    fun emptyLine() = textComponent("")
}