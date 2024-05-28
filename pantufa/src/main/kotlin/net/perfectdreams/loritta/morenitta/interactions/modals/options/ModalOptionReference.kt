package net.perfectdreams.loritta.morenitta.interactions.modals.options

import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import java.util.*

sealed class ModalOptionReference<T>

sealed class DiscordModalOptionReference<T>(
    val label: String,
    val required: Boolean
) : ModalOptionReference<T>() {
    val name = UUID.randomUUID().toString()

    abstract fun get(option: ModalMapping): T

    abstract fun toJDA(): ActionComponent
}

class StringDiscordModalOptionReference<T>(
    label: String,
    val style: TextInputStyle,
    val value: String?,
    required: Boolean
) : DiscordModalOptionReference<T>(label, required) {
    override fun get(option: ModalMapping): T {
        return option.asString as T
    }

    override fun toJDA() = TextInput.create(
        name,
        label,
        style
    ).setValue(value).build()
}

// ===[ BUILDERS ]===
fun modalString(label: String, style: TextInputStyle, value: String? = null) = StringDiscordModalOptionReference<String>(
    label,
    style,
    value,
    true
)

fun optionalModalString(label: String, style: TextInputStyle, value: String? = null) = StringDiscordModalOptionReference<String?>(
    label,
    style,
    value,
    false
)