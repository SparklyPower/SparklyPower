package net.perfectdreams.loritta.morenitta.interactions.commands.options

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object : ApplicationCommandOptions() {}
    }

    val registeredOptions = mutableListOf<OptionReference<*>>()

    fun string(name: String, description: String) = StringDiscordOptionReference<String>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalString(name: String, description: String) = StringDiscordOptionReference<String?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun long(name: String, description: String) = LongDiscordOptionReference<Long>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalLong(name: String, description: String) = LongDiscordOptionReference<Long?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun user(name: String, description: String) = UserDiscordOptionReference<UserAndMember>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalUser(name: String, description: String) = UserDiscordOptionReference<UserAndMember?>(name, description, false)
        .also { registeredOptions.add(it) }
}