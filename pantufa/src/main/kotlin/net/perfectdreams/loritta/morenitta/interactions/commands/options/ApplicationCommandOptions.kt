package net.perfectdreams.loritta.morenitta.interactions.commands.options

import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object : ApplicationCommandOptions() {}
    }

    val registeredOptions = mutableListOf<OptionReference<*>>()

    fun string(name: String, description: String) = StringDiscordOptionReference<String>(name, description, true)
        .also { registeredOptions.add(it) }

    fun string(name: String, description: String, builder: StringDiscordOptionReference<String>.() -> (Unit) = {}) = StringDiscordOptionReference<String>(name, description, true)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun optionalString(name: String, description: String) = StringDiscordOptionReference<String?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun optionalString(name: String, description: String, builder: StringDiscordOptionReference<String?>.() -> (Unit) = {}) = StringDiscordOptionReference<String?>(name, description, false)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun integer(name: String, description: String) = IntDiscordOptionReference<Int>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalInteger(name: String, description: String) = IntDiscordOptionReference<Int?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun long(name: String, description: String) = LongDiscordOptionReference<Long>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalLong(name: String, description: String) = LongDiscordOptionReference<Long?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun user(name: String, description: String) = UserDiscordOptionReference<UserAndMember>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalUser(name: String, description: String) = UserDiscordOptionReference<UserAndMember?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun channel(name: String, description: String) = ChannelDiscordOptionReference<GuildChannel>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalChannel(name: String, description: String) = ChannelDiscordOptionReference<GuildChannel?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun attachment(name: String, description: String) = AttachmentDiscordOptionReference<Attachment>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalAttachment(name: String, description: String) = AttachmentDiscordOptionReference<Attachment?>(name, description, false)
        .also { registeredOptions.add(it) }
}