package net.sparklypower.common.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun String.fromLegacySectionToTextComponent() = LegacyComponentSerializer.legacySection().deserialize(this)