package net.perfectdreams.dreamcore.utils.commands.options

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun word(name: String, suggests: SuggestsBlock = null) = WordCommandOptionBuilder(name, suggests)
    fun quotableString(name: String, suggests: SuggestsBlock = null) = QuotableStringCommandOptionBuilder(name, suggests)
    fun greedyString(name: String, suggests: SuggestsBlock = null) = GreedyStringCommandOptionBuilder(name, suggests)
    fun optionalGreedyString(name: String, suggests: SuggestsBlock = null) = OptionalGreedyStringCommandOptionBuilder(name, suggests)

    fun boolean(name: String, suggests: SuggestsBlock = null) = BooleanCommandOptionBuilder(name, suggests)

    fun double(name: String, suggests: SuggestsBlock = null) = DoubleCommandOptionBuilder(name, suggests)
    fun double(name: String, min: Double, suggests: SuggestsBlock = null) = DoubleMinCommandOptionBuilder(name, min, suggests)
    fun double(name: String, min: Double, max: Double, suggests: SuggestsBlock = null) = DoubleMinMaxCommandOptionBuilder(name, min, max, suggests)

    fun player(name: String, suggests: SuggestsBlock = null) = PlayerCommandOptionBuilder(name, suggests)

    /* val arguments = mutableListOf<CommandOption<*>>()

    fun string(name: String, description: String) = argument<String>(
        CommandOptionType.String,
        name,
        description
    )

    fun optionalString(name: String, description: String) = argument<String?>(
        CommandOptionType.NullableString,
        name,
        description
    )

    fun integer(name: String, description: String) = argument<Long>(
        CommandOptionType.Integer,
        name,
        description
    )

    fun optionalInteger(name: String, description: String) = argument<Long?>(
        CommandOptionType.NullableInteger,
        name,
        description
    )

    fun number(name: String, description: String) = argument<Double>(
        CommandOptionType.Number,
        name,
        description
    )

    fun optionalNumber(name: String, description: String) = argument<Double?>(
        CommandOptionType.NullableNumber,
        name,
        description
    )

    fun boolean(name: String, description: String) = argument<Boolean>(
        CommandOptionType.Bool,
        name,
        description
    )

    fun optionalBoolean(name: String, description: String) = argument<Boolean?>(
        CommandOptionType.NullableBool,
        name,
        description
    )

    fun user(name: String, description: String) = argument<User>(
        CommandOptionType.User,
        name,
        description
    )

    fun optionalUser(name: String, description: String) = argument<User?>(
        CommandOptionType.NullableUser,
        name,
        description
    )

    fun channel(name: String, description: String) = argument<Channel>(
        CommandOptionType.Channel,
        name,
        description
    )

    fun optionalChannel(name: String, description: String) = argument<Channel?>(
        CommandOptionType.NullableChannel,
        name,
        description
    )

    fun role(name: String, description: String) = argument<Role>(
        CommandOptionType.Role,
        name,
        description
    )

    fun optionalRole(name: String, description: String) = argument<Role?>(
        CommandOptionType.NullableRole,
        name,
        description
    ) */

    inline fun <reified T> CommandOptionBuilder<T>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = build()

        arguments.add(option)
        return option
    }
}