package net.perfectdreams.dreamchat.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreamchat.DreamChat
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerEditBookEvent

class CharacterBlockerListener(val m: DreamChat) : Listener {
    // Disable anvil rename
    @EventHandler(priority = EventPriority.LOWEST)
    fun onAnvilPrepare(e: PrepareAnvilEvent) {
        val itemDisplayName = e.result?.itemMeta?.displayName

        if (itemDisplayName != null && hasBlockedCharacters(itemDisplayName))
            e.result = null
    }

    // Disable chat
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncChatEvent) {
        val s = PlainTextComponentSerializer.plainText().serialize(e.message())

        if (hasBlockedCharacters(s))
            e.isCancelled = true
    }

    // Disable chat (This is required because ChatListener uses AsyncPlayerChatEvent)
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncPlayerChatEvent) {
        if (hasBlockedCharacters(e.message))
            e.isCancelled = true
    }

    // Disable commands
    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (hasBlockedCharacters(e.message))
            e.isCancelled = true
    }

    // Disable signs
    @EventHandler(priority = EventPriority.LOWEST)
    fun onSign(e: SignChangeEvent) {
        for (line in e.lines()) {
            val s = PlainTextComponentSerializer.plainText().serialize(line)

            if (hasBlockedCharacters(s)) {
                e.isCancelled = true
                return
            }
        }
    }

    // Disable books
    @EventHandler(priority = EventPriority.LOWEST)
    fun onBook(e: PlayerEditBookEvent) {
        for (page in e.newBookMeta.pages()) {
            val s = PlainTextComponentSerializer.plainText().serialize(page)

            if (hasBlockedCharacters(s)) {
                e.isCancelled = true
                return
            }
        }
    }

    private fun hasBlockedCharacters(input: String): Boolean {
        val emojis = m.replacers.values

        for (blocker in m.blockers) {
            val matches = blocker.findAll(input)
            for (match in matches) {
                if (match.value !in emojis && m.emojis.any { match.value == it.character })
                    return true
            }
        }

        return false
    }
}