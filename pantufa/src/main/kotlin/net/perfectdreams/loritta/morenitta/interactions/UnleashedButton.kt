package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.util.*

object UnleashedButton {
    // Just a dummy component ID, this SHOULD HOPEFULLY be replaced by a proper ID down the road when used with InteractivityManager
    private const val DO_NOT_USE_THIS_COMPONENT_ID = "DO_NOT_USE_THIS"
    // Just a dummy component url, this SHOULD HOPEFULLY be replaced by a proper URL down the road
    private const val DO_NOT_USE_THIS_LINK_URL = "https://loritta.website/?you-forgot-to-use-withUrl-on-the-unleashed-button"

    fun of(
        style: ButtonStyle,
        label: String? = null,
        emoji: Emote
    ): Button = of(style, label, emoji.toJDA())

    fun of(
        style: ButtonStyle,
        label: String? = null,
        emoji: Emoji? = null
    ): Button {
        if (style == ButtonStyle.LINK)
            return Button.of(style, DO_NOT_USE_THIS_LINK_URL, label, emoji)
        return Button.of(style, DO_NOT_USE_THIS_COMPONENT_ID + ":" + UUID.randomUUID().toString(), label, emoji)
    }
}