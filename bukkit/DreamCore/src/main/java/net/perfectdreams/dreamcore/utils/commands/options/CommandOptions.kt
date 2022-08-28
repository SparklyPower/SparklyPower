package net.perfectdreams.dreamcore.utils.commands.options

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun word(name: String, suggests: SuggestsBlock = null) = WordCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun optionalWord(name: String, suggests: SuggestsBlock = null) = OptionalWordCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun quotableString(name: String, suggests: SuggestsBlock = null) = QuotableStringCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun greedyString(name: String, suggests: SuggestsBlock = null) = GreedyStringCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun optionalGreedyString(name: String, suggests: SuggestsBlock = null) = OptionalGreedyStringCommandOptionBuilder(name, suggests)
        .let { register(it) }

    fun boolean(name: String, suggests: SuggestsBlock = null) = BooleanCommandOptionBuilder(name, suggests)
        .let { register(it) }

    fun integer(name: String, suggests: SuggestsBlock = null) = IntegerCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun integer(name: String, min: Int, suggests: SuggestsBlock = null) = IntegerMinCommandOptionBuilder(name, min, suggests)
        .let { register(it) }
    fun integer(name: String, min: Int, max: Int, suggests: SuggestsBlock = null) = IntegerMinMaxCommandOptionBuilder(name, min, max, suggests)
        .let { register(it) }
    fun optionalInteger(name: String, suggests: SuggestsBlock = null) = OptionalIntegerCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun optionalInteger(name: String, min: Int, suggests: SuggestsBlock = null) = OptionalIntegerMinCommandOptionBuilder(name, min, suggests)
        .let { register(it) }
    fun optionalInteger(name: String, min: Int, max: Int, suggests: SuggestsBlock = null) = OptionalIntegerMinMaxCommandOptionBuilder(name, min, max, suggests)
        .let { register(it) }

    fun double(name: String, suggests: SuggestsBlock = null) = DoubleCommandOptionBuilder(name, suggests)
        .let { register(it) }
    fun double(name: String, min: Double, suggests: SuggestsBlock = null) = DoubleMinCommandOptionBuilder(name, min, suggests)
        .let { register(it) }
    fun double(name: String, min: Double, max: Double, suggests: SuggestsBlock = null) = DoubleMinMaxCommandOptionBuilder(name, min, max, suggests)
        .let { register(it) }

    fun player(name: String, suggests: SuggestsBlock = null) = PlayerCommandOptionBuilder(name, suggests)
        .let { register(it) }

    inline fun <reified T> register(optionBuilder: CommandOptionBuilder<T>): CommandOption<T> {
        if (arguments.any { it.name == optionBuilder.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = optionBuilder.build()

        arguments.add(option)
        return option
    }
}