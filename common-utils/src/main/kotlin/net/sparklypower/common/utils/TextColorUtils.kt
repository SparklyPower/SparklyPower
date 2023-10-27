package net.sparklypower.common.utils

import net.kyori.adventure.text.format.TextColor

fun TextColor.toLegacySection(): String {
    return buildString {
        append("§x")
        for (ch in this@toLegacySection.asHexString().removePrefix("#")) {
            append("§")
            append(ch)
        }
    }
}

fun TextColor.toLegacyAmpsersand(): String {
    return buildString {
        append("&x")
        for (ch in this@toLegacyAmpsersand.asHexString().removePrefix("#")) {
            append("&")
            append(ch)
        }
    }
}