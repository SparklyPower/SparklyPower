package net.perfectdreams.dreamcore.utils.adventure

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.meta.ItemMeta

// Inspired by https://github.com/KyoriPowered/adventure/tree/main/4/extra-kotlin/src/main/kotlin/net/kyori/adventure/extra/kotlin
// But I tried to make it more ergonomic to use, because the original version is kinda... "bad" to use, because it doesn't
// really feel like a Kotlin DSL :(
fun textComponent(block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().apply(block).build()
fun textComponent(text: String, block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().content(text).apply(block).build()

fun ItemMeta.displayNameWithoutDecorations(block: TextComponent.Builder.() -> (Unit) = {}) = displayName(
    textComponent {
        decoration(TextDecoration.ITALIC, false)
        block.invoke(this)
    }
)

fun ItemMeta.displayNameWithoutDecorations(content: String, block: TextComponent.Builder.() -> (Unit) = {}) = displayName(
    textComponent(content) {
        decoration(TextDecoration.ITALIC, false)
        color(NamedTextColor.WHITE)
        block.invoke(this)
    }
)

fun ItemMeta.lore(block: LoreBuilder.() -> (Unit) = {}) = lore(LoreBuilder().apply(block).components)

fun TextComponent.Builder.append(color: TextColor, content: String, block: TextComponent.Builder.() -> (Unit) = {}) = append(
    Component.text()
        .color(color)
        .content(content)
        .apply(block)
)

fun TextComponent.Builder.append(content: String, block: TextComponent.Builder.() -> (Unit) = {}) = append(
    Component.text()
        .content(content)
        .apply(block)
)

fun TextComponent.Builder.suggestCommandOnClick(command: String) {
    clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
}

fun TextComponent.Builder.runCommandOnClick(command: String) {
    clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
}

fun TextComponent.Builder.hoverText(block: TextComponent.Builder.() -> (Unit) = {}) {
    hoverEvent(HoverEvent.showText(textComponent(block)))
}

fun TextComponent.Builder.appendCommand(command: String) = append(
    textComponent {
        content(command)
        color(NamedTextColor.GOLD)
        suggestCommandOnClick(command)
        hoverText {
            content("Clique para executar o comando!")
        }
    }
)

fun Audience.sendTextComponent(block: TextComponent.Builder.() -> (Unit) = {}) = sendMessage(textComponent(block))