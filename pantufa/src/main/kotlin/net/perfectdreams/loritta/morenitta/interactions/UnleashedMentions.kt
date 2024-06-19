package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji

class UnleashedMentions(
    users: List<User>,
    channels: List<GuildChannel>,
    customEmojis: List<CustomEmoji>,
    roles: List<Role>
) {
    val users: List<User>
        get() = mutableUsers
    val channels: List<GuildChannel>
        get() = mutableChannels
    val customEmojis: List<CustomEmoji>
        get() = mutableCustomEmojis
    val roles: List<Role>
        get() = mutableRoles

    private val mutableUsers = users.toMutableList()
    private val mutableChannels = channels.toMutableList()
    private val mutableCustomEmojis = customEmojis.toMutableList()
    private val mutableRoles = roles.toMutableList()

    fun injectUser(user: User) {
        mutableUsers.add(user)
    }
}