package net.perfectdreams.dreamcore.utils.commands.options

open class CommandOptions {
    companion object {
        val NO_OPTIONS = object: CommandOptions() {}
    }

    val arguments = mutableListOf<CommandOption<*>>()

    fun word(name: String, suggests: SuggestsBlock = null) = WordCommandOptionBuilder(name, suggests)
    fun optionalWord(name: String, suggests: SuggestsBlock = null) = OptionalWordCommandOptionBuilder(name, suggests)
    fun quotableString(name: String, suggests: SuggestsBlock = null) = QuotableStringCommandOptionBuilder(name, suggests)
    fun greedyString(name: String, suggests: SuggestsBlock = null) = GreedyStringCommandOptionBuilder(name, suggests)
    fun optionalGreedyString(name: String, suggests: SuggestsBlock = null) = OptionalGreedyStringCommandOptionBuilder(name, suggests)

    fun boolean(name: String, suggests: SuggestsBlock = null) = BooleanCommandOptionBuilder(name, suggests)

    fun integer(name: String, suggests: SuggestsBlock = null) = IntegerCommandOptionBuilder(name, suggests)
    fun integer(name: String, min: Int, suggests: SuggestsBlock = null) = IntegerMinCommandOptionBuilder(name, min, suggests)
    fun integer(name: String, min: Int, max: Int, suggests: SuggestsBlock = null) = IntegerMinMaxCommandOptionBuilder(name, min, max, suggests)
    fun optionalInteger(name: String, suggests: SuggestsBlock = null) = OptionalIntegerCommandOptionBuilder(name, suggests)
    fun optionalInteger(name: String, min: Int, suggests: SuggestsBlock = null) = OptionalIntegerMinCommandOptionBuilder(name, min, suggests)
    fun optionalInteger(name: String, min: Int, max: Int, suggests: SuggestsBlock = null) = OptionalIntegerMinMaxCommandOptionBuilder(name, min, max, suggests)

    fun double(name: String, suggests: SuggestsBlock = null) = DoubleCommandOptionBuilder(name, suggests)
    fun double(name: String, min: Double, suggests: SuggestsBlock = null) = DoubleMinCommandOptionBuilder(name, min, suggests)
    fun double(name: String, min: Double, max: Double, suggests: SuggestsBlock = null) = DoubleMinMaxCommandOptionBuilder(name, min, max, suggests)

    fun player(name: String, suggests: SuggestsBlock = null) = PlayerCommandOptionBuilder(name, suggests)

    inline fun <reified T> CommandOptionBuilder<T>.register(): CommandOption<T> {
        if (arguments.any { it.name == this.name })
            throw IllegalArgumentException("Duplicate argument!")

        val option = build()

        arguments.add(option)
        return option
    }
}