package net.perfectdreams.dreamcore.utils.commands.options

import org.bukkit.entity.Player

sealed class CommandOptionBuilder<T>(
    val name: String,
    var suggests: SuggestsBlock = null
) {
    abstract fun build(): CommandOption<T>
}
class WordCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<String>(name, suggests) {
    override fun build() = WordCommandOption(name, suggests)
}
class OptionalWordCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<String?>(name, suggests) {
    override fun build() = OptionalWordCommandOption(name, suggests)
}
class QuotableStringCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<String>(name, suggests) {
    override fun build() = QuotableStringCommandOption(name, suggests)
}
class GreedyStringCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<String>(name, suggests) {
    override fun build() = GreedyStringCommandOption(name, suggests)
}
class OptionalGreedyStringCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<String?>(name, suggests) {
    override fun build() = OptionalGreedyStringCommandOption(name, suggests)
}
class BooleanCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<Boolean>(name, suggests) {
    override fun build() = BooleanCommandOption(name, suggests)
}
class IntegerCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<Int>(name, suggests) {
    override fun build() = IntegerCommandOption(name, suggests)
}
class IntegerMinCommandOptionBuilder(name: String, val min: Int, suggests: SuggestsBlock) : CommandOptionBuilder<Int>(name, suggests) {
    override fun build() = IntegerMinCommandOption(name, min, suggests)
}
class IntegerMinMaxCommandOptionBuilder(name: String, val min: Int, val max: Int, suggests: SuggestsBlock) : CommandOptionBuilder<Int>(name, suggests) {
    override fun build() = IntegerMinMaxCommandOption(name, min, max, suggests)
}
class OptionalIntegerCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<Int?>(name, suggests) {
    override fun build() = OptionalIntegerCommandOption(name, suggests)
}
class OptionalIntegerMinCommandOptionBuilder(name: String, val min: Int, suggests: SuggestsBlock) : CommandOptionBuilder<Int?>(name, suggests) {
    override fun build() = OptionalIntegerMinCommandOption(name, min, suggests)
}
class OptionalIntegerMinMaxCommandOptionBuilder(name: String, val min: Int, val max: Int, suggests: SuggestsBlock) : CommandOptionBuilder<Int?>(name, suggests) {
    override fun build() = OptionalIntegerMinMaxCommandOption(name, min, max, suggests)
}
class DoubleCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<Double>(name, suggests) {
    override fun build() = DoubleCommandOption(name, suggests)
}
class DoubleMinCommandOptionBuilder(name: String, val min: Double, suggests: SuggestsBlock) : CommandOptionBuilder<Double>(name, suggests) {
    override fun build() = DoubleMinCommandOption(name, min, suggests)
}
class DoubleMinMaxCommandOptionBuilder(name: String, val min: Double, val max: Double, suggests: SuggestsBlock) : CommandOptionBuilder<Double>(name, suggests) {
    override fun build() = DoubleMinMaxCommandOption(name, min, max, suggests)
}
class PlayerCommandOptionBuilder(name: String, suggests: SuggestsBlock) : CommandOptionBuilder<Player>(name, suggests) {
    override fun build() = PlayerCommandOption(name, suggests)
}