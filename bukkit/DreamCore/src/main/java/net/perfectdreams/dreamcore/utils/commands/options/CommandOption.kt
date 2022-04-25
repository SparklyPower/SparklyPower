package net.perfectdreams.dreamcore.utils.commands.options

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import org.bukkit.entity.Player

typealias SuggestsBlock = ((CommandContext, SuggestionsBuilder) -> (Unit))?
sealed class CommandOption<T>(val name: String, val optional: Boolean, val suggestsBlock: SuggestsBlock)
class WordCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class OptionalWordCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String?>(name, true, suggestsBlock)
class QuotableStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class GreedyStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class OptionalGreedyStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String?>(name, true, suggestsBlock)
class BooleanCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Boolean>(name, false, suggestsBlock)
class IntegerCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Int>(name, false, suggestsBlock)
class IntegerMinCommandOption(name: String, val min: Int, suggestsBlock: SuggestsBlock) : CommandOption<Int>(name, false, suggestsBlock)
class IntegerMinMaxCommandOption(name: String, val min: Int, val max: Int, suggestsBlock: SuggestsBlock) : CommandOption<Int>(name, false, suggestsBlock)
class OptionalIntegerCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Int?>(name, true, suggestsBlock)
class OptionalIntegerMinCommandOption(name: String, val min: Int, suggestsBlock: SuggestsBlock) : CommandOption<Int?>(name, true, suggestsBlock)
class OptionalIntegerMinMaxCommandOption(name: String, val min: Int, val max: Int, suggestsBlock: SuggestsBlock) : CommandOption<Int?>(name, true, suggestsBlock)
class DoubleCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class DoubleMinCommandOption(name: String, val min: Double, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class DoubleMinMaxCommandOption(name: String, val min: Double, val max: Double, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class PlayerCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Player>(name, false, suggestsBlock)