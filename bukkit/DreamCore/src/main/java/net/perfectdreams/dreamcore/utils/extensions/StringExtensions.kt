package net.perfectdreams.dreamcore.utils.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.TextUtils

fun String.centralize() = TextUtils.getCenteredMessage(this)
fun String.centralizeHeader() = TextUtils.getCenteredHeader(this)

val String.asComponent get() = Component.text(this)

val String.asBoldComponent get() = this.asComponent.decorations(mapOf(
    TextDecoration.BOLD to TextDecoration.State.TRUE,
    TextDecoration.ITALIC to TextDecoration.State.FALSE
))