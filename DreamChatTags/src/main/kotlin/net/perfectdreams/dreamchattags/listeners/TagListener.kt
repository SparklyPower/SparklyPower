package net.perfectdreams.dreamchattags.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamchattags.DreamChatTags
import net.perfectdreams.dreamcore.utils.colorize
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamChatTags) : Listener {
    @EventHandler
    fun onTag(e: ApplyPlayerTagsEvent) {
        val playerTags = m.config.getList("players." + e.player.uniqueId.toString())

        playerTags?.forEach { tag ->
            val tags = m.config.getConfigurationSection("tags.$tag")

            if (tags != null) {
                val small = (tags.getString("small") ?: "?").colorize()
                val tagName = (tags.getString("name")  ?: "?").colorize()
                val description = tags.getStringList("description").map { it.colorize() }
                val suggestCommand = tags.getString("suggestCommand")
                val expiresAt = tags.getLong("expires-at", -1)

                if (System.currentTimeMillis() >= expiresAt)
                    e.tags.add(
                        PlayerTag(
                            small,
                            tagName,
                            description,
                            suggestCommand
                        )
                    )
            }
        }
    }
}