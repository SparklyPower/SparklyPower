package net.perfectdreams.loritta.morenitta.utils.extensions

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote

fun Emote.toJDA() = when (this) {
    is DiscordEmote -> Emoji.fromCustom(
        this.name,
        this.id,
        this.animated
    )

    is UnicodeEmote -> Emoji.fromUnicode(this.name)
}

fun CommandCategory.pretty(): String {
    return when (this) {
        CommandCategory.ECONOMY -> "Economia"
        CommandCategory.MAGIC -> "Mágica"
        CommandCategory.MISC -> "Diversos"
        CommandCategory.MINECRAFT -> "Minecraft"
        CommandCategory.MODERATION -> "Moderação"
        CommandCategory.UTILS -> "Utilidades"
        else -> "Desconhecido"
    }
}