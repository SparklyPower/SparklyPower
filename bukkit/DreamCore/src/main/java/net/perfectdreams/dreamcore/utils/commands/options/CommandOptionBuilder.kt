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