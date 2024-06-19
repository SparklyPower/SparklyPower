package net.perfectdreams.loritta.morenitta.interactions

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.pantufa.api.commands.styled
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class InteractivityManager {
    companion object {
        val INTERACTION_INVALIDATION_DELAY = 5.minutes
    }

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val buttonInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ComponentContext) -> (Unit)>()
        .asMap()
    val selectMenuInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ComponentContext, List<String>) -> (Unit)>()
        .asMap()
    val modalCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ModalContext, ModalArguments) -> (Unit)>()
        .asMap()

    fun buttonForUser(
        targetUser: User,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, style, label, builder, callback)

    fun buttonForUser(
        targetUser: User,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, button, callback)

    fun buttonForUser(
        targetUserId: Long,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        style,
        label,
        builder
    ) {
        if (targetUserId != it.user.idLong) {
            it.reply(true) {
                styled(
                    "Espere um pouquinho... Você não é <@${targetUserId}>! Isso não é para você, sai daqui!",
                    "<:pantufa_bonk:1028160322990776331>"
                )
            }
            return@button
        }

        callback.invoke(it)
    }

    fun buttonForUser(
        targetUserId: Long,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        button
    ) {
        if (targetUserId != it.user.idLong) {
            it.reply(true) {
                styled(
                    "Espere um pouquinho... Você não é <@${targetUserId}>! Isso não é para você, sai daqui!",
                    "<:pantufa_bonk:1028160322990776331>"
                )
            }
            return@button
        }
        callback.invoke(it)
    }

    fun button(
        style: ButtonStyle,
        label: String,
        builder: (JDAButtonBuilder).() -> Unit = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        UnleashedButton.of(style, label, null)
            .let {
                JDAButtonBuilder(it).apply(builder).button
            },
        callback
    )

    fun button(
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = callback
        return button
            .withId(UnleashedComponentId(buttonId).toString())
    }

    fun disabledButton(
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {}
    ): Button {
        val buttonId = UUID.randomUUID()
        return Button.of(style, "disabled:$buttonId", label)
            .let {
                JDAButtonBuilder(it).apply(builder)
                    .apply {
                        disabled = true
                    }
                    .button
            }
    }

    fun stringSelectMenuForUser(
        targetUser: User,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenuForUser(targetUser.idLong, builder, callback)

    fun stringSelectMenuForUser(
        targetUserId: Long,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenu(
        builder
    ) { context, strings ->
        if (targetUserId != context.user.idLong) {
            context.reply(true) {
                styled(
                    "Espere um pouquinho... Você não é <@${targetUserId}>! Isso não é para você, sai daqui!",
                    "<:pantufa_bonk:1028160322990776331>"
                )
            }
            return@stringSelectMenu
        }

        callback.invoke(context, strings)
    }

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenu(
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ): StringSelectMenu {
        val buttonId = UUID.randomUUID()
        selectMenuInteractionCallbacks[buttonId] = callback
        return StringSelectMenu.create(UnleashedComponentId(buttonId).toString())
            .apply(builder)
            .build()
    }

    class JDAButtonBuilder(internal var button: Button) {
        // https://youtrack.jetbrains.com/issue/KT-6519
        @get:JvmSynthetic // Hide from Java callers
        var emoji: Emoji
            @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
            get() = throw UnsupportedOperationException()
            set(value) {
                button = button.withEmoji(value)
            }

        @get:JvmSynthetic // Hide from Java callers
        var loriEmoji: Emote
            @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
            get() = throw UnsupportedOperationException()
            set(value) {
                emoji = value.toJDA()
            }

        var disabled
            get() = button.isDisabled
            set(value) {
                this.button = button.withDisabled(value)
            }
    }
}

